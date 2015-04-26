package com.orange.game.model.manager.photo;

import java.util.Date;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.photo.Photo;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.service.DBService;
import com.orange.network.game.protocol.model.PhotoProtos.PBUserPhoto;

public class PhotoManager extends CommonManager {
	// thread-safe singleton implementation
	private static PhotoManager manager = new PhotoManager();
	private PhotoManager() {
		
	}
	
	public static PhotoManager getInstance() {
		return manager;
	}

	public Photo addOrUpdatePhoto(PBUserPhoto pbUserPhoto) {
		
		ObjectId photoId = null;
		if (ObjectId.isValid(pbUserPhoto.getPhotoId())){
			photoId = new ObjectId(pbUserPhoto.getPhotoId());
		}
		
		BasicDBObject query = new BasicDBObject();
		if (photoId != null){
			// update photo
			query.put("_id", photoId);
		}
		else{
			// insert or update by photo URL
			query.put(DBConstants.F_URL, pbUserPhoto.getUrl());
		}
		
		BasicDBObject update = new BasicDBObject();
		
		BasicDBObject setValue = new BasicDBObject();
		setValue.put(DBConstants.F_URL, pbUserPhoto.getUrl());
		setValue.put(DBConstants.F_MODIFY_DATE, new Date());
		setValue.put(DBConstants.F_HEIGHT,pbUserPhoto.getHeight());
		setValue.put(DBConstants.F_WIDTH, pbUserPhoto.getWidth());
		
		update.put("$set", setValue);
		if (pbUserPhoto.getTagsCount() > 0){
			update.put("$addToSet", new BasicDBObject(DBConstants.F_TAG, pbUserPhoto.getTagsList()));
		}
		if (pbUserPhoto.hasName() && pbUserPhoto.getName().length() > 0){
			BasicDBList nameList = new BasicDBList();
			nameList.add(pbUserPhoto.getName());
			update.put("$addToSet", new BasicDBObject(DBConstants.F_KEYWORD, nameList));
		}
		
		DBObject obj = DBService.getInstance().getMongoDBClient().findAndModifyUpsert(
				DBConstants.T_PHOTO, 
				query, 
				update);
		
		return new Photo(obj);
	}
}
