package org.vminkov.feed.controller;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
public class UsersService {

	@Autowired
	private UsersManager usersManager;

	@Autowired
	private Datastore ds;

	@RequestMapping(method = RequestMethod.POST, value = "/user")
	public String register(User toberegistered) {
		if (!toberegistered.getUsername().matches("^[a-z0-9_-]{3,15}$")) {
			throw new RuntimeException("invalid username");
		}
		if (!toberegistered.getPassword().matches("^[a-z0-9_-]{6,15}$")) {
			throw new RuntimeException("invalid password");
		}

		ds.save(toberegistered);

		return this.usersManager.addSession(toberegistered);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/user")
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
}