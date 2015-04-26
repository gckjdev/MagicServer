package com.orange.game.model.manager.feed;
import org.apache.log4j.Logger;

import com.orange.common.mongodb.MongoDBClient;

//The class response to manage the all feed and hot feed.
public class FeedManager {
	public static final Logger log = Logger.getLogger(AllFeedManager.class
			.getName());
	protected static MongoDBClient mongoClient = null;
	
	public static void setMongoDBClient(MongoDBClient client){
		mongoClient = client;
	}
	
	protected FeedManager() {
		super();
	}	
}
