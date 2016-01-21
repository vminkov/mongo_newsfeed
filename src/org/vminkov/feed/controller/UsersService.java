package org.vminkov.feed.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.DB;
import com.mongodb.DBCollection;

@RestController
public class UsersService {
	@Autowired
	private DB mongoDB;
	
	@Autowired
	private UsersManager usersManager;

	@Autowired
	private Datastore ds;

	@RequestMapping(method = RequestMethod.POST, value = "/user")
	public SessionData register(@RequestBody User toberegistered) {
		if (!toberegistered.getUsername().matches("^[a-z0-9_-]{3,15}$")) {
			throw new RuntimeException("invalid username");
		}
		if (!toberegistered.getPassword().matches("^[a-z0-9_-]{6,15}$")) {
			throw new RuntimeException("invalid password");
		}
		if (toberegistered.getAvatar() == null || toberegistered.getAvatar().isEmpty()) {
			throw new RuntimeException("invalid avatar");
		}

		if (ds.find(User.class).filter("username =", toberegistered.getUsername()).get() != null) {
			throw new RuntimeException("username already taken");
		}

		ds.save(toberegistered);

		return new SessionData(this.usersManager.addSession(toberegistered));
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
				
				DBCollection collection = this.mongoDB.getCollection("user");
				
				
				user.setAvatar(new String(bytes));
				this.ds.updateFirst(this.ds.createQuery(User.class).filter("_id =", user.get_id()), user, false);

				return ResponseEntity.created(new URI("/index.html")).build();
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
}