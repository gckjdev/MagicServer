package com.orange.game.model.service;

import com.orange.common.db.MongoDBExecutor;
import com.orange.game.constants.DBConstants;

public class DBService extends MongoDBExecutor {

	@Override
	public String getDBName() {
		return DBConstants.D_GAME;
	}
	
	// thread-safe singleton implementation
	private static DBService manager = new DBService();     
	private DBService(){		
		super();
	} 	    	
	public static DBService getInstance() { 
		return manager; 
	}	
}

