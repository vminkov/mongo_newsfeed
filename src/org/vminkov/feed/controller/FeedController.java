package org.vminkov.feed.controller;

import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedController {
	private static final int PAGE_SIZE = 20;
	@Autowired
	private Datastore datastore;
	
	@RequestMapping("/")
	public String getText(){
		return "<br><div style='text-align:center;'>"
				+ "<h3>Mongo db </h3>Veliko</div><br><br>";
	}
	
	@RequestMapping("/feed/{page}")
	public List<Message> getFeed(@PathVariable("page") Integer pageNum){
		Query<Message> find = datastore.find(Message.class);
		Query<Message> offset = find.offset(pageNum * PAGE_SIZE);
		Query<Message> limit = offset.limit(PAGE_SIZE);
		return limit.asList();
	}
}
