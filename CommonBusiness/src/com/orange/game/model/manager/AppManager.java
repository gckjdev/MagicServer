package com.orange.game.model.manager;

import java.util.HashMap;

import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.App;
import com.orange.game.model.dao.UserAction;

public class AppManager extends CommonManager {
	public static App getApp(MongoDBClient mongoClient, String appId) {
		DBObject object = mongoClient.findOne(DBConstants.T_APP, DBConstants.F_APPID, appId);
		if (object == null ) {
			return null;
		}
		App app =  new App(object);
		return app;
	}

    /*
	public static HashMap<String, Integer> getStatistic(
			MongoDBClient mongoClient, String appId) {
		//user count 
		int userCount = (int) UserManager.countUser(mongoClient, appId);
		
		//draw count
		int drawCount = (int) UserManager.countUserAction(mongoClient, appId,UserAction.TYPE_DRAW);
		
		//draw to user
		int drawToUserCount = (int) UserManager.countUserAction(mongoClient, appId,UserAction.TYPE_DRAW_TO_USER);
		
		//guess count
		int guessCount = (int) UserManager.countUserAction(mongoClient, appId,UserAction.TYPE_GUESS);
		
		//comment count
		int commentCount = (int) UserManager.countUserAction(mongoClient, appId,UserAction.TYPE_COMMENT);
		
		//chat count
		int message = (int) MessageManager.countMessage(mongoClient, appId);
		
		HashMap<String, Integer>map = new HashMap<String, Integer>();
		
		map.put(ServiceConstant.PARA_USER_NUMBER, userCount);
		map.put(ServiceConstant.PARA_DRAW_NUMBER, drawCount);
		map.put(ServiceConstant.PARA_DRAW_TO_USER_NUMBER, drawToUserCount);
		map.put(ServiceConstant.PARA_GUESS_NUMBER, guessCount);
		map.put(ServiceConstant.PARA_COMMENT_NUMBER, commentCount);
		map.put(ServiceConstant.PARA_MESSAGE_NUMBER, message);
		
		return map;
	}
	*/
}
