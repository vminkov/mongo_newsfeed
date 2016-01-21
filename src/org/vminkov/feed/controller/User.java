package org.vminkov.feed.controller;

import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity("user")
public class User {

	@Id
	private String _id;
	
	private String username;
	private String password;
	private String avatar;

	@Reference
	private List<User> silenced;
	
	public User(String _id, String username, String password, String avatar, List<User> silenced) {
		this._id = _id;
		this.username = username;
		this.password = password;
		this.avatar = avatar;
		this.silenced = silenced;
	}
	
	public User() {}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<User> getSilenced() {
		return silenced;
	}

	public void setSilenced(List<User> silenced) {
		this.silenced = silenced;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		
		if(!(obj instanceof User)){
			return false;
		}
		 
		User another = (User) obj;

		return this.username != null && this.password != null
				&& this.username.equals(another.username) && this.password.equals(another.password); 
	}
}
