package com.orange.game.traffic.service;

import com.orange.common.db.MongoDBExecutor;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;

public class GameDBService extends MongoDBExecutor {

	@Override
	public String getDBName() {
		return DBConstants.D_GAME;
	}
	
	// thread-safe singleton implementation
	private static GameDBService manager = new GameDBService();     
	private GameDBService(){		
		super();
	} 	    	
	public static GameDBService getInstance() { 
		return manager; 
	}	
}
