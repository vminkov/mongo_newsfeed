package org.vminkov.feed.services;

import java.util.ArrayList;
import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.vminkov.feed.beans.Message;
import org.vminkov.feed.beans.User;
import org.vminkov.feed.controller.UsersManager;
import org.vminkov.feed.services.FeedService.PostMessageData;

@RestController
public class MessageService {
	@Autowired
	private UsersManager usersManager;
	@Autowired
	private Datastore ds;

	@RequestMapping(value = "/message", method = RequestMethod.POST)
	public ResponseEntity<String> postMessage(@RequestHeader("Authorization") String sessionId,
			@RequestBody PostMessageData message) {
		User user = usersManager.validateSession(sessionId);

		this.ds.save(new Message(message.text, user, new Date(), new ArrayList<>(), null));
		return ResponseEntity.ok("{'success': true}");
	}
	
	@RequestMapping(value = "/message/{id}/like", method = RequestMethod.POST)
	public ResponseEntity<String> likeMessage(@RequestHeader("Authorization") String sessionId,
			@PathVariable("id") String messageId) {
		User user = usersManager.validateSession(sessionId);

		this.ds.updateFirst(this.ds.createQuery(Message.class).field("_id").equal(new ObjectId(messageId)), 
				this.ds.createUpdateOperations(Message.class).add("likes", user));
		
		return new ResponseEntity<String>("{'status': 'succsess'}", HttpStatus.OK);
	}
	
	@RequestMapping(value = "/message/{id}/unlike", method = RequestMethod.POST)
	public ResponseEntity<String> unlikeMessage(@RequestHeader("Authorization") String sessionId,
			@PathVariable("id") String messageId) {
		User user = usersManager.validateSession(sessionId);

		this.ds.updateFirst(this.ds.createQuery(Message.class).field("_id").equal(new ObjectId(messageId)), 
				this.ds.createUpdateOperations(Message.class).removeAll("likes", user));
		
		return new ResponseEntity<String>("{'status': 'succsess'}", HttpStatus.OK);
	}
}
