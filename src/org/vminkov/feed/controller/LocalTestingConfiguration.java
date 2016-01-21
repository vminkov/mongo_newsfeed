package org.vminkov.feed.controller;

import java.net.UnknownHostException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

@Configuration
public class LocalTestingConfiguration {
	private static final String SERVER = "localhost";
	private static final String DATABASE_NAME = "newsfeed";

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
		try {
			mongoClient = new MongoClient(SERVER);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		instance = morphia.createDatastore(mongoClient, DATABASE_NAME);
		return instance;
	}

	@Bean
	public UsersManager getUsersManager() {
		return new UsersManager();
	}

	@Bean
	public DB getMongoDB() {
		MongoClient mongoClient;
		try {
			mongoClient = new MongoClient(SERVER);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		return mongoClient.getDB("newsfeed");
	}
}