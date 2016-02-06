package org.vminkov.feed.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.vminkov.feed.beans.User;

@Controller
public class UsersManager {
	private Map<String, User> sessions = new HashMap<>();
	
	public String addSession(User user){
		String sessionId = UUID.randomUUID().toString();
		this.sessions.put(sessionId, user);
		return sessionId;
	}

	public User validateSession(String sessionId){
		User user = this.sessions.get(sessionId);
		if(user == null){
			throw new RuntimeException("invalid session");
		}
		
		return user;
	}
	
	public String getSessionId(User user)
	{
		for(Map.Entry<String, User> some : this.sessions.entrySet()){
			if(some.getValue().equals(user)){
				return some.getKey();
			}
		}
		
		return null;
	}
}
