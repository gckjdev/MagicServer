package com.orange.game.model.manager;

import java.util.Date;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.traffic.server.GameServer;

public class UserGameStatusManager {
	
	public static final Logger log = Logger.getLogger(UserGameStatusManager.class
			.getName());
	
	public static void userEnterGame(MongoDBClient mongoClient, String userId, 
			String serverAddress, int serverPort, String serverId,			
			int sessionId, String gameId
			){
		
		if (userId == null)
			return;
		
		// step 1: update user_game_status
		int gameStatus = DBConstants.C_GAME_STATUS_ONLINE;	
		
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_UID, new ObjectId(userId));
		
		BasicDBObject update = new BasicDBObject();
		update.put(DBConstants.F_UID, new ObjectId(userId));
		update.put(DBConstants.F_SERVER_ADDRESS, serverAddress);
		update.put(DBConstants.F_SERVER_PORT, serverPort);
		update.put(DBConstants.F_SERVER_ID, serverId);
		update.put(DBConstants.F_SESSION_ID, sessionId);
		update.put(DBConstants.F_GAME_ID, gameId);
		update.put(DBConstants.F_MODIFY_DATE, new Date());
		update.put(DBConstants.F_STATUS, gameStatus);
		
		mongoClient.updateOrInsert(DBConstants.T_USER_GAME_STATUS, query, update);
		
		// step 2: update user friend
		RelationManager.updateUserGameStatus(mongoClient, userId, gameStatus, serverId, sessionId);
	}
	
	public static void userQuitGame(MongoDBClient mongoClient, String userId){

		if (userId == null)
			return;
		
		// step 1: update user_game_status
		int gameStatus = DBConstants.C_GAME_STATUS_OFFLINE;			

		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_UID, new ObjectId(userId));
		
		/*
		BasicDBObject update = new BasicDBObject();
		update.put(DBConstants.F_UID, userId);		
		update.put(DBConstants.F_MODIFY_DATE, new Date());
		update.put(DBConstants.F_STATUS, gameStatus);
		*/
		
		mongoClient.remove(DBConstants.T_USER_GAME_STATUS, query);
		
		// step 2: update user friend
		RelationManager.updateUserGameStatus(mongoClient, userId, gameStatus, null, -1);
		
	}

	public static void clearAllUserGameStatus(MongoDBClient mongoClient) {
		
		String serverId = GameServer.getServerId();
		if (serverId == null){
			log.warn("<clearAllUserGameStatus> but serverId is null");
			return;
		}
		
		log.info("<clearAllUserGameStatus> serverId="+serverId);
		
		// step 1 remove all user related to serverId
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_SERVER_ID, serverId);		
		mongoClient.remove(DBConstants.T_USER_GAME_STATUS, query);
		
		// step 2 remove follower related to serverId
		RelationManager.clearUserGameStatusByServerId(mongoClient, serverId);
	}
}
