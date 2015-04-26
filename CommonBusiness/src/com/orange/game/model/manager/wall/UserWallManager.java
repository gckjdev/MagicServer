package com.orange.game.model.manager.wall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.wall.UserWall;
import com.orange.game.model.manager.CommonManager;
import com.orange.network.game.protocol.model.DrawProtos.PBWall;

public class UserWallManager extends CommonManager {

	public static UserWall createUserWall(MongoDBClient mongoClient, PBWall pbWall, String backgroundImageUrl) {

		UserWall userWall = UserWall.fromPBWall(pbWall, true);
		if (backgroundImageUrl != null){
			userWall.setBackground(backgroundImageUrl);
		}
		boolean result = mongoClient.insert(DBConstants.T_USER_WALL, userWall.getDbObject());
		if (result == false)
			return null;
				
		log.info("<createUserWall> success, data="+userWall.getDbObject().toString());		
		return userWall;
	}

	public static UserWall getWallById(MongoDBClient mongoClient, String wallId) {
		if (StringUtil.isEmpty(wallId)){
			log.info("<getWallById> wallId null");					
			return null;
		}
		
		DBObject obj = mongoClient.findOneByObjectId(DBConstants.T_USER_WALL, wallId);
		if (obj == null){
			log.info("<getWallById> wallId="+wallId+" not found");					
			return null;
		}
		
		return new UserWall(obj);
	}

	public static List<UserWall> getWallListByUser(MongoDBClient mongoClient,
			String userId) {
		if (StringUtil.isEmpty(userId)){
			log.info("<getWallListByUser> userId null");					
			return Collections.emptyList();
		}
		
		DBCursor cursor = mongoClient.find(DBConstants.T_USER_WALL, DBConstants.F_FOREIGN_USER_ID, userId);
		if (cursor == null){
			return Collections.emptyList();			
		}
		
		List<UserWall> retList = new ArrayList<UserWall>();
		while (cursor.hasNext()){
			DBObject obj = cursor.next();
			retList.add(new UserWall(obj));			
		}
		cursor.close();		
		return retList;
	}

	public static UserWall updateUserWall(MongoDBClient mongoClient,
			String wallId, PBWall pbWall, String backgroundImageUrl) {

		if (StringUtil.isEmpty(wallId)){
			log.warn("<updateUserWall> wallId is null or empty");
			return null;
		}
		
		log.info("<updateUserWall> wall="+pbWall.toString());
		
		UserWall userWall = UserWall.fromPBWall(pbWall, false);
		if (backgroundImageUrl != null){
			userWall.setBackground(backgroundImageUrl);
		}
		
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(wallId));
		BasicDBObject update = new BasicDBObject();

		DBObject updateValue = userWall.getDbObject();
		update.put("$set", updateValue);
		
		log.info("<updateUserWall> query="+query.toString()+",update="+update.toString());
				
		mongoClient.updateAll(DBConstants.T_USER_WALL, query, update);		
		return userWall;
	}

}
