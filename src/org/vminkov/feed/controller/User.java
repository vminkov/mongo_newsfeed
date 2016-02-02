package org.vminkov.feed.controller;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity("user")
public class User {

	@Id
	private ObjectId _id;
	
	private String username;
	@JsonIgnore
	private String password;
	private byte[] avatar;

	@Reference
	private List<User> silenced;
	
	public User(ObjectId _id, String username, String password, byte[] avatar, List<User> silenced) {
		this._id = _id;
		this.username = username;
		this.password = password;
		this.avatar = avatar;
		this.silenced = silenced;
	}
	
	public User() {}

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
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

	public byte[] getAvatar() {
		return avatar;
	}

	public void setAvatar(byte[] avatar) {
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
