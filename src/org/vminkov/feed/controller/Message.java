package org.vminkov.feed.controller;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity("message")
public class Message {
	@Id
	private ObjectId  _id;

	@Reference
	private User author;

	private String text; 
	private Date date;

	@Reference
	private List<User> likes;

	public Message(String text, User author, Date sentDate, List<User> likes, ObjectId _id) {
		this.text = text;
		this.author = author;
		this.date = sentDate;
		this.likes = likes;
		this._id = _id;
	}

	public Message() {
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<User> getLikes() {
		return likes;
	}

	public void setLikes(List<User> likes) {
		this.likes = likes;
	}
	
	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public ObjectId get_id() {
		return _id;
	}
}