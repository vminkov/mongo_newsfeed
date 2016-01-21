package org.vminkov.feed.controller;

import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedService {
	private static final int PAGE_SIZE = 20;
	@Autowired
	private Datastore datastore;
	@Autowired
	private UsersManager usersManager;
	
	@RequestMapping("/feed/{page}")
	public List<Message> getFeed(@RequestHeader("Authorization") String sessionId, 
			@PathVariable("page") Integer pageNum){
		User user = usersManager.validateSession(sessionId);
		
		Query<Message> limit = datastore.find(Message.class)
				.order("-date")
				.offset(pageNum * PAGE_SIZE)
				.limit(PAGE_SIZE);
		return limit
				.asList();
	}
}
