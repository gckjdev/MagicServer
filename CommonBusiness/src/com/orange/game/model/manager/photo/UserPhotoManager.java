package com.orange.game.model.manager.photo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.photo.Photo;
import com.orange.game.model.dao.photo.UserPhoto;
import com.orange.game.model.dao.photo.UserPhotoData;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.service.DBService;
import com.orange.network.game.protocol.model.PhotoProtos.PBUserPhoto;

public class UserPhotoManager extends CommonManager {

	// thread-safe singleton implementation
	private static UserPhotoManager manager = new UserPhotoManager();
	private UserPhotoManager() {
		
	}
	
	public static UserPhotoManager getInstance() {
		return manager;
	}

	public void addUserPhoto(MongoDBClient mongoClient, String userId,
			String url, int type, boolean isAbsolutePath) {

		BasicDBObject obj = new BasicDBObject();
		obj.put(DBConstants.F_UID, userId);
		obj.put(DBConstants.F_PATH, url);
		obj.put(DBConstants.F_TYPE, type);
		obj.put(DBConstants.F_IS_ABSOLUTE, isAbsolutePath);
		obj.put(DBConstants.F_CREATE_DATE, new Date());
		
		log.info("<addUserPhoto> data="+obj.toString());
		mongoClient.insert(DBConstants.T_USER_PHOTO, obj);
	}

	public UserPhotoData addOrUpdateUserPhoto(String userId, 
			String photoId,
			PBUserPhoto pbUserPhoto) {
		
		BasicDBObject query = new BasicDBObject();

		// insert or update by user id and photo id
		query.put(DBConstants.F_UID, userId);
		query.put(DBConstants.F_PHOTO_ID, photoId);
		
		BasicDBObject update = new BasicDBObject();
		
		BasicDBObject setValue = new BasicDBObject();
		setValue.put(DBConstants.F_URL, pbUserPhoto.getUrl());
		setValue.put(DBConstants.F_MODIFY_DATE, new Date());
		setValue.put(DBConstants.F_TAG, pbUserPhoto.getTagsList());
		setValue.put(DBConstants.F_NAME, pbUserPhoto.getName());
		setValue.put(DBConstants.F_USAGE, pbUserPhoto.getUsage());
		setValue.put(DBConstants.F_HEIGHT,pbUserPhoto.getHeight());
		setValue.put(DBConstants.F_WIDTH, pbUserPhoto.getWidth());
		
		update.put("$set", setValue);
		
		DBObject obj = DBService.getInstance().getMongoDBClient().findAndModifyUpsert(
				DBConstants.T_USER_PHOTO_DATA, 
				query, 
				update);
		
		return new UserPhotoData(obj);		
	}

	public boolean removeUserPhoto(String userId, String userhotoId) {
		
		BasicDBObject deleteObject = new BasicDBObject();
		deleteObject.put(DBConstants.F_OBJECT_ID, new ObjectId(userhotoId));
		
		return DBService.getInstance().getMongoDBClient().remove(DBConstants.T_USER_PHOTO_DATA, deleteObject);
		
	}

	
	public boolean userPhotoIsExist(String userId,String photoId){
		
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_UID, userId);
		query.put(DBConstants.F_PHOTO_ID, photoId);
		
		DBObject dbObject = DBService.getInstance().getMongoDBClient().findOne(DBConstants.T_USER_PHOTO_DATA, query);
		if (dbObject != null) 
			return true;
		else
			return false;
	}
	
	
	
	public List<UserPhotoData> getList(String userId,int usage,String[] tags, int offset, int limit
			) {
		
		if (tags == null) {
			log.warn("<getList> but tags is null");
			return Collections.emptyList();
		}
		
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_UID, userId);
		query.put(DBConstants.F_USAGE, usage);
		BasicDBList list = new BasicDBList();
        Collections.addAll(list, tags);
//		for(String tag:tags){
//			list.add(tag);
//		}

		DBObject inQuery = new BasicDBObject();
		inQuery.put("$in", list);
		query.put(DBConstants.F_TAG, inQuery);
		
		
		log.info("<getList> query = "+query);
		DBCursor dbCursor = DBService.getInstance().getMongoDBClient().find(DBConstants.T_USER_PHOTO_DATA, query, null, offset, limit);
		if (dbCursor!=null) {
			List<UserPhotoData> userPhotoDataList = new ArrayList<UserPhotoData>();
			while (dbCursor.hasNext()) {
				DBObject dbObject = dbCursor.next();
				UserPhotoData userPhotoData = new UserPhotoData(dbObject);
				userPhotoDataList.add(userPhotoData);
			}
			dbCursor.close();
			return userPhotoDataList;
		}
		return Collections.emptyList();
	}
	

}
