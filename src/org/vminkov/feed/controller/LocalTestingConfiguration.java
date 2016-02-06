package org.vminkov.feed.controller;

import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vminkov.feed.beans.Message;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Configuration
public class LocalTestingConfiguration {
	private static final String USER = "user";
	private static final String SERVER = "localhost";
	private static final String DATABASE_NAME = "newsfeed";
	private static final String MESSAGE = "message";

	@Bean
	public Morphia getMorphia() {
		Morphia morphia = new Morphia();
		morphia.map(Message.class);

		return morphia;
	}

	@Bean
	public Datastore getDatastore(Morphia morphia) {
		Datastore instance;

		MongoClient mongoClient;
		mongoClient = new MongoClient(SERVER);
		instance = morphia.createDatastore(mongoClient, DATABASE_NAME);
		return instance;
	}

	@Bean
	public UsersManager getUsersManager() {
		return new UsersManager();
	}

	@SuppressWarnings("resource")
	@Bean
	public MongoDatabase getMongoDB() {
		MongoClient mongoClient;
		mongoClient = new MongoClient(SERVER);
		return mongoClient.getDatabase(DATABASE_NAME);
	}

	@Bean(name="messagesCollection")
	public MongoCollection<Document> getMessagesCollection(MongoDatabase mongoDB){
		return mongoDB.getCollection(MESSAGE);
	}
	
	@Bean(name="usersCollection")
	public MongoCollection<Document> getUsersCollection(MongoDatabase mongoDB){
		return mongoDB.getCollection(USER);
	}
}