package org.vminkov.feed.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.client.MongoCollection;

@RestController
public class FeedService {
	private static final int PAGE_SIZE = 20;
	@Autowired
	private UsersManager usersManager;
	@Autowired
	private Datastore ds;

	@Autowired
	@Qualifier("messagesCollection")
	private MongoCollection<Document> collection;

	@Autowired
	@Qualifier("usersCollection")
	private MongoCollection<Document> usersCollection;

	@RequestMapping("/feed/{page}")
	public List<FeedMessage> getFeed(@RequestHeader("Authorization") String sessionId,
			@PathVariable("page") Integer pageNum) {
		User user = usersManager.validateSession(sessionId);

		Query<Message> messagesQuery = this.ds.find(Message.class)
				.filter("author nin", 
						this.ds.find(User.class).field("_id").equal(user.get_id()).get().getSilenced())
				.order("-date").offset(PAGE_SIZE * pageNum)
				.limit(PAGE_SIZE);
		List<Message> messages = messagesQuery.asList();

		return convertMessages(user, messages);
	}

	private List<FeedMessage> convertMessages(User currentUser, List<Message> dbMessages) {
		List<FeedMessage> restMess = new ArrayList<>();
		for (Message mess : dbMessages) {
			boolean liked = mess.getLikes().contains(currentUser);
			
			if(mess.getAuthor() == null){
				System.err.println("null author for " + mess.get_id());
				continue;
			}
			
			restMess.add(new FeedMessage(mess.getAuthor(), mess.getDate(), mess.getText(), liked, mess.getLikes(), mess.get_id()));
		}

		return restMess;
	}

	static class FeedMessage {
		@JsonProperty
		String author;
		@JsonProperty
		String authorId;
		@JsonProperty
		String _id;
		@JsonProperty
		Date date;
		@JsonProperty
		String text;
		@JsonProperty
		boolean liked;
		@JsonProperty
		List<String> likes;

		public FeedMessage() {
		}

		public FeedMessage(User author, Date date, String text, boolean liked, List<User> likes, ObjectId _id) {
			if(author != null){
				this.author = author.getUsername();
				this.authorId = author.get_id().toString();
			}
			
			this.date = date;
			this.text = text;
			this.liked = liked;
			this._id = _id.toString();
			
			this.likes = new ArrayList<>();
			for(User whoLikedit : likes){
				this.likes.add(whoLikedit.getUsername());
			}
		}
	}

	@RequestMapping(value = "/feed", method = RequestMethod.POST)
	public ResponseEntity<String> postMessage(@RequestHeader("Authorization") String sessionId,
			@RequestBody PostMessageData message) {
		User user = usersManager.validateSession(sessionId);

		this.ds.save(new Message(message.text, user, new Date(), new ArrayList<>(), null));
		return ResponseEntity.ok("{'success': true}");
	}

	static class PostMessageData {
		@JsonProperty
		String text;

		public PostMessageData() {
		}
	}
}
