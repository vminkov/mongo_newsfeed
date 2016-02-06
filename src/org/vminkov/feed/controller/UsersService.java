package org.vminkov.feed.controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.BasicDBObject;
import com.mongodb.DBRef;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@RestController
public class UsersService {
	@Autowired
	private MongoDatabase mongoDB;

	@Autowired
	private UsersManager usersManager;

	@Autowired
	private Datastore ds;

	private static final byte[] DEFAULT_AVATAR = initDefaultAvatar();

	static byte[] initDefaultAvatar() {
		try {
			return Base64.getEncoder().encode(FileUtils.readFileToByteArray(new File("mongo/no_avatar.jpg")));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/user/{silencedUserId}/silence")
	public ResponseEntity<String> silence(@RequestHeader("Authorization") String sessionId,
			@PathVariable("silencedUserId") String silencedUserId) {
		User user = usersManager.validateSession(sessionId);

		if (!user.get_id().equals(new ObjectId(silencedUserId))) {
			this.ds.updateFirst(this.ds.createQuery(User.class).field("_id").equal(user.get_id()),
					this.ds.createUpdateOperations(User.class).add("silenced",
							new DBRef("user", new ObjectId(silencedUserId))));
		}

		return new ResponseEntity<String>("{'status': 'succsess'}", HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/user/data/batch")
	public ResponseEntity<Map<String, ProfileData>> getUsersDataBatch(@RequestHeader("Authorization") String sessionId,
			@RequestParam("ids[]") String[] ids) {
		Map<String, ProfileData> result = new HashMap<String, ProfileData>();

		usersManager.validateSession(sessionId);

		Set<ObjectId> objIds = new HashSet<>();
		for (String id : ids) {
			objIds.add(new ObjectId(id));
		}

		MongoCollection<Document> userCollection = this.mongoDB.getCollection("user");
		MongoCursor<Document> usersIterator = userCollection
				.find(new Document("_id", new Document("$in", objIds)), Document.class).iterator();
		Document current = null;
		while ((current = usersIterator.tryNext()) != null) {
			String avatarBase64;
			Object avatarBytes = current.get("avatar");

			if (avatarBytes instanceof String) {
				avatarBase64 = (String) avatarBytes;
			} else if (avatarBytes instanceof Binary) {
				avatarBase64 = new String(((Binary) avatarBytes).getData());
			} else {
				avatarBase64 = avatarBytes.toString();
			}

			result.put(current.getObjectId("_id").toString(),
					new ProfileData(current.getString("username"), avatarBase64, null, null));
		}

		return new ResponseEntity<Map<String, ProfileData>>(result, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/user/profile")
	public ResponseEntity<ProfileData> getUserData(@RequestHeader("Authorization") String sessionId,
			@RequestParam("id") String id) {
		ProfileData result = null;

		User thisUser = usersManager.validateSession(sessionId);

		MongoCollection<Document> userCollection = this.mongoDB.getCollection("user");
		DBRef userDBRef = new DBRef("user", new ObjectId(id));
		Document current = userCollection.find(new Document("_id", new ObjectId(id)), Document.class).first();

		if (current != null) {
			MongoCollection<Document> messagesCollection = this.mongoDB.getCollection("message");
			double rating = getUserRating(userDBRef, userCollection, messagesCollection);

			long totalPosts = messagesCollection.count(new Document("author", userDBRef));
			String avatarBase64;
			Object avatarBytes = current.get("avatar");

			if (avatarBytes instanceof String) {
				avatarBase64 = (String) avatarBytes;
			} else if (avatarBytes instanceof Binary) {
				avatarBase64 = new String(((Binary) avatarBytes).getData());
			} else {
				avatarBase64 = avatarBytes.toString();
			}

			result = new ProfileData(current.getString("username"), avatarBase64, totalPosts, rating);

			if (thisUser.get_id().equals(new ObjectId(id))) {
				long posts24_4 = getPostsInHours(id, messagesCollection, 0, 4);

				long posts4_8 = getPostsInHours(id, messagesCollection, 4, 8);
				long posts8_16 = getPostsInHours(id, messagesCollection, 8, 16);
				long posts16_24 = getPostsInHours(id, messagesCollection, 16, 0);

				result.postsStats = new Float[] { new Float(posts24_4), new Float(posts4_8), new Float(posts8_16),
						new Float(posts16_24) };
			}
		}

		return new ResponseEntity<ProfileData>(result, HttpStatus.OK);
	}

	private double getUserRating(DBRef userDBRef, MongoCollection<Document> userCollection, 
			MongoCollection<Document> messagesCollection) {
		double rating = 0;

		long silencedBy = userCollection.count(new Document("silenced", new Document("$elemMatch", userDBRef)));

		List<Document> rules = new ArrayList<>();
		rules.add(new Document("author", new Document("$ne", userDBRef)));
		rules.add(new Document("likes", new Document("$elemMatch", userDBRef)));
		long totalLikes = messagesCollection.count(new Document("$and", rules));

		if (silencedBy == 0) {
			rating = Double.MAX_VALUE;
		} else {
			rating = (double) totalLikes / (silencedBy * silencedBy);
		}
		return rating;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/user/profile/ratings")
	public List<UserRatingsData> getAllRatings(@RequestHeader("Authorization") String sessionId){
		usersManager.validateSession(sessionId);
		
		List<UserRatingsData> result = new ArrayList<>();
		
		MongoCollection<Document> userCollection = this.mongoDB.getCollection("user");
		MongoCollection<Document> messagesCollection = this.mongoDB.getCollection("message");
		
		Map<Object, Integer> usersSilencedByOthersTotal = new HashMap<>();
		Map<Object, Integer> usersPostsLikedByOthersTotal = new HashMap<>();
		
		/* AGGREGATION PIPELINE */
		List<BasicDBObject> pipeline = new ArrayList<>();
		pipeline.add(new BasicDBObject("$unwind", new BasicDBObject("path", "$silenced")));
		BasicDBObject groupBy = new BasicDBObject("_id", "$silenced");
		groupBy.append("count", new BasicDBObject("$sum", 1));
		pipeline.add(new BasicDBObject("$group", groupBy));
		
		AggregateIterable<Document> aggregateIterable = userCollection.aggregate(pipeline);
		MongoCursor<Document> eachUserIsSilenced = aggregateIterable.iterator();
		while (eachUserIsSilenced.hasNext()) {
			Document thisUserIsSilenced = eachUserIsSilenced.next();
			Integer thisUserSilencedTimes = thisUserIsSilenced.getInteger("count");
			DBRef userRef = (DBRef) thisUserIsSilenced.get("_id");
			
			usersSilencedByOthersTotal.put(userRef.getId(), thisUserSilencedTimes);
		}
		
		/* MAP REDUCE */
		String mapLikes = "function(){emit(this.author, {likes: (this.likes ? this.likes.length : 0)});}";
		String reduceLikes = "function(key, values){" + "sum = 0;" + "for(var i in values){" + "sum += values[i].likes;"
				+ "}" + "return {userRef: key, likes: sum};" + "}";
		MongoCursor iterator = messagesCollection.mapReduce(mapLikes, reduceLikes).iterator();
		while (iterator.hasNext()) {
			Document likesDoc = (Document) iterator.next();
			Double likes = ((Document) likesDoc.get("value")).getDouble("likes");
			DBRef userRef = (DBRef) likesDoc.get("_id");
			usersPostsLikedByOthersTotal.put(userRef.getId(), (int) Math.round(likes));
		}

		ArrayList<Document> allUsers = userCollection.find().into(new ArrayList<>());
		
		for (Document userDoc : allUsers) {
			double rating;
			Integer likes = usersPostsLikedByOthersTotal.get(userDoc.get("_id"));
			Integer silenced = IfNull.withDefault(usersSilencedByOthersTotal.get(userDoc.get("_id")), 0).get();
			
			if (likes != null && silenced != null) {
				rating = (silenced < 1) ? Double.MAX_VALUE : ((double) likes) / (silenced * silenced);
			} else {
				rating = Double.MIN_VALUE;
			}

			result.add(new UserRatingsData(userDoc.get("_id").toString(), userDoc.get("username").toString(), rating));
		}
		
		Collections.sort(result);
		
		return result;
	}

	private long getPostsInHours(String id, MongoCollection<Document> messagesCollection, int hourFrom,
			int hourTo) {
		
		// does not use indices because of $where
		return messagesCollection
				.count(new Document("$where", "function(){return this.author.$id == \"" + id + "\" && this.date.getHours() >= "
						+ hourFrom + (hourTo == 0 ? "" : " && this.date.getHours() < " + hourTo) + "}"));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/user")
	public SessionData register(@RequestBody LogInData toberegistered) {
		if (!toberegistered.username.matches("^[a-z0-9_-]{3,15}$")) {
			throw new RuntimeException("invalid username");
		}
		if (!toberegistered.password.matches("^[a-z0-9_-]{6,15}$")) {
			throw new RuntimeException("invalid password");
		}

		if (ds.find(User.class).filter("username =", toberegistered.username).get() != null) {
			throw new RuntimeException("username already taken");
		}

		User user = new User(null, toberegistered.username, toberegistered.password, DEFAULT_AVATAR, new ArrayList<>());
		ds.save(user);

		return new SessionData(this.usersManager.addSession(user));
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/session")
	public SessionData logIn(@RequestBody LogInData data) {
		Query<User> find = this.ds.find(User.class, "username", data.username);
		User user = find.get();

		IfNull.throwRE(user, new RuntimeException("invalid username"));
//		if (user == null) {
//			throw new RuntimeException("invalid username");
//		}

		if (!user.getPassword().equals(data.password)) {
			throw new RuntimeException("invalid password");
		}

		return new SessionData(this.usersManager.addSession(user));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/session")
	public ResponseEntity<Void> validateSession(@RequestHeader("Authorization") String sessionId)
			throws URISyntaxException {
		if (this.usersManager.validateSession(sessionId) == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok().build();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(method = RequestMethod.POST, value = "/upload")
	public ResponseEntity uploadAvatar(@RequestParam("sessionId") String sessionId,
			@RequestParam("file") MultipartFile file) throws IOException, URISyntaxException {
		User user = usersManager.validateSession(sessionId);

		if (!file.isEmpty()) {
			try {
				byte[] bytes = file.getBytes();
				MongoCollection<Document> userCollection = this.mongoDB.getCollection("user");
				userCollection.updateOne(new Document("username", user.getUsername()),
						new Document("$set", new Document("avatar", Base64.getEncoder().encode(bytes))));

				return new ResponseEntity(
						"<html><head><meta http-equiv=\"refresh\" content=\"0; url=/index.html\" /></head></html>",
						HttpStatus.MOVED_PERMANENTLY);
			} catch (IOException e) {
				return new ResponseEntity("{'status': 'failed to upload => " + e.getMessage() + "'}",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity("{'status': 'failed to upload because the file was empty'}",
					HttpStatus.BAD_REQUEST);
		}
	}

	static class SessionData {
		@JsonProperty
		String sessionId;

		public SessionData(String sessId) {
			this.sessionId = sessId;
		}

		public SessionData() {
		}
	}

	static class LogInData {
		@JsonProperty
		String username;

		@JsonProperty
		String password;

		public LogInData() {
		}
	}

	static class ProfileData {
		@JsonProperty
		String username;
		@JsonProperty
		String avatar;
		@JsonProperty
		Long totalPosts;
		@JsonProperty
		String rating;
		@JsonProperty
		Float[] postsStats;

		public ProfileData(String username, String avatar, Long totalPosts, Double rating) {
			this.username = username;
			this.avatar = avatar;
			this.totalPosts = totalPosts;

			this.rating = ratingAsString(rating);
		}

		public ProfileData() {
		}
	}

	private static String ratingAsString(Double rating) {
		if (rating == null) {
			return null;
		} else if (rating == Double.MAX_VALUE) {
			return "Highest";
		} else if (rating == Double.MIN_VALUE) {
			return "N/A";
		} else {
			return rating.toString();
		}
	}
	
	static class UserRatingsData implements Comparable<UserRatingsData> {
		@JsonProperty
		String username;
		@JsonProperty
		String rating;
		@JsonProperty
		String id;
		
		@JsonIgnore
		Double ratingNumber;

		public UserRatingsData(String id, String username, Double rating) {
			this.id = id;
			this.username = username;

			this.ratingNumber = rating;
			
			this.rating = ratingAsString(rating);
		}

		public UserRatingsData() {
		}

		@Override
		public int compareTo(UserRatingsData o) {
			return -this.ratingNumber.compareTo(o.ratingNumber);
		}
	}
}