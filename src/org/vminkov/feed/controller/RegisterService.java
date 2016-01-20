package org.vminkov.feed.controller;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterService {

	@Autowired
	private Datastore ds;

	@RequestMapping(method = RequestMethod.POST, value = "/user")
	public User register(User toberegistered) {
		if (!toberegistered.getUsername().matches("^[a-z0-9_-]{3,15}$")) {
			throw new RuntimeException("invalid username");
		}
		if (!toberegistered.getPassword().matches("^[a-z0-9_-]{6,15}$")) {
			throw new RuntimeException("invalid password");
		}

		Key<User> save = ds.save(toberegistered);
		toberegistered.set_id(save.getId().toString());
		return toberegistered;
	}
}
