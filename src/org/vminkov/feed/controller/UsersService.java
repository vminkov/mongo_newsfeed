package org.vminkov.feed.controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.BasicDBList;
import com.mongodb.DBRef;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@RestController
public class UsersService {	@Autowired
	private MongoDatabase mongoDB;

	@Autowired
	private UsersManager usersManager;

	@Autowired
	private Datastore ds;

	private static final byte[] DEFAULT_AVATAR = initDefaultAvatar();
	static byte[] initDefaultAvatar(){
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
	public ResponseEntity<Map<String, User>> getUsersDataBatch(@RequestHeader("Authorization") String sessionId,
			@RequestParam("ids[]") String[] ids) {
		Map<String, User> result = new HashMap<String, User>();

		usersManager.validateSession(sessionId);

		Set<ObjectId> objIds = new HashSet<>();
		for (String id : ids) {
			objIds.add(new ObjectId(id));
		}

		MongoCollection<Document> userCollection = this.mongoDB.getCollection("user");
		MongoCursor<Document> usersIterator = userCollection
				.find(new Document("_id", new Document("$in", objIds)), Document.class).limit(20).iterator();
		Document current = null;
		while ((current = usersIterator.tryNext()) != null) {
			byte[] avatarBase64;
			Object avatarBytes = current.get("avatar");

			if (avatarBytes instanceof String) {
				avatarBase64 = ((String) avatarBytes).getBytes();
			} else if (avatarBytes instanceof Binary) {
				avatarBase64 = ((Binary) avatarBytes).getData();
			} else {
				avatarBase64 = avatarBytes.toString().getBytes();
			}

			result.put(current.getObjectId("_id").toString(),
					new User(null, current.getString("username"), null, avatarBase64, null));
		}

		return new ResponseEntity<Map<String, User>>(result, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/user/profile")
	public ResponseEntity<ProfileData> getUserData(@RequestHeader("Authorization") String sessionId,
			@RequestParam("id") String id) {
		ProfileData result = null;

		User thisUser = usersManager.validateSession(sessionId);

		MongoCollection<Document> userCollection = this.mongoDB.getCollection("user");
		DBRef userObjectId =  new DBRef("user", new ObjectId(id));
		Document current = userCollection.find(new Document("_id", new ObjectId(id)), Document.class)
				.first();

		if (current != null) {
			double rating = 0;
			MongoCollection<Document> messagesCollection = this.mongoDB.getCollection("message");

			long silencedBy = userCollection.count(new Document("silenced", new Document("$elemMatch", userObjectId)));
			long totalLikes = messagesCollection.count(new Document("likes", new Document("$elemMatch", userObjectId)));
			long totalPosts = messagesCollection.count(new Document("author", userObjectId));
			
			if(silencedBy == 0){
				rating = Double.MAX_VALUE;
			} else {
				rating = totalLikes / (silencedBy * silencedBy);
			}
			
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
			
			if(thisUser.get_id().equals(new ObjectId(id))){
				BasicDBList dateFilter = new BasicDBList();
				dateFilter.add(new Document("$where", "return this.date.getHour() >= 0 && this.date.getHour() < 4"));
				dateFilter.add(new Document("_id", userObjectId));
				long posts24_4 = messagesCollection.count(new Document("$and", dateFilter));
				
				long posts4_8 = messagesCollection.count(new Document("date", new Document("$where", userObjectId)));
				long posts8_16 = messagesCollection.count(new Document("date", new Document("$where", userObjectId)));
				long posts16_24 = messagesCollection.count(new Document("date", new Document("$where", userObjectId)));
				
				result.timeStats = null;
			}
		}

		return new ResponseEntity<ProfileData>(result, HttpStatus.OK);
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

		if (user == null) {
			throw new RuntimeException("invalid username");
		}

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
		Float[] timeStats;

		public ProfileData(String username, String avatar, Long totalPosts, Double rating) {
			this.username = username;
			this.avatar = avatar;
			this.totalPosts = totalPosts;
			
			if(rating != Double.MAX_VALUE){
				this.rating = rating.toString();
			}else{
				this.rating = "Highest";
			}
		}

		public ProfileData() {
		}
	}
}