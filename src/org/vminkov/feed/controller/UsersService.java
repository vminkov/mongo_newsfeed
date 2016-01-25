package org.vminkov.feed.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;

import org.bson.Document;
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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@RestController
public class UsersService {
	@Autowired
	private MongoDatabase mongoDB;

	@Autowired
	private UsersManager usersManager;

	@Autowired
	private Datastore ds;

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

		User user = new User(null, toberegistered.username, toberegistered.password, "aidebe64==", new ArrayList<>());
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
}