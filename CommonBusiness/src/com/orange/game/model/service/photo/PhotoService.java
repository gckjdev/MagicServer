package com.orange.game.model.service.photo;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.orange.game.model.dao.photo.Photo;
import com.orange.game.model.dao.photo.UserPhotoData;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.photo.PhotoManager;
import com.orange.game.model.manager.photo.UserPhotoIndexManager;
import com.orange.game.model.manager.photo.UserPhotoManager;
import com.orange.network.game.protocol.model.PhotoProtos.PBUserPhoto;
import com.sun.corba.se.spi.ior.Identifiable;

public class PhotoService {
	
	public static final Logger log = Logger.getLogger(PhotoService.class
			.getName());
	
	// thread-safe singleton implementation
	private static PhotoService service = new PhotoService();
	private PhotoService() {
		
	}
	
	public static PhotoService getInstance() {
		return service;
	}

//	public void addUserPhoto(MongoDBClient mongoClient, String userId,
//			String url, int type, boolean isAbsolutePath) {
//
//		BasicDBObject obj = new BasicDBObject();
//		obj.put(DBConstants.F_UID, userId);
//		obj.put(DBConstants.F_PATH, url);
//		obj.put(DBConstants.F_TYPE, type);
//		obj.put(DBConstants.F_IS_ABSOLUTE, isAbsolutePath);
//		obj.put(DBConstants.F_CREATE_DATE, new Date());
//		
//		log.info("<addUserPhoto> data="+obj.toString());
//		mongoClient.insert(DBConstants.T_USER_PHOTO, obj);
//	}

	public UserPhotoData addUserPhoto(String userId, PBUserPhoto pbUserPhoto) {
		
		if (!UserPhotoData.isValidUsage(pbUserPhoto.getUsage())){
			return null;
		}
		
		// create or update Photo table
		Photo photo = PhotoManager.getInstance().addOrUpdatePhoto(pbUserPhoto);
		if (photo == null){
			log.warn("<addUserPhoto> but fail to add or update photo, pbUserPhoto="+pbUserPhoto.toString());
			return null;
		}
		
		
		boolean isExist = UserPhotoManager.getInstance().userPhotoIsExist(userId, photo.getPhotoId());
		log.info("<addUserPhoto> user photo is exist = "+isExist);
		
		// create o update user photo table
		UserPhotoData userPhotoData = UserPhotoManager.getInstance().addOrUpdateUserPhoto(userId, photo.getPhotoId(), pbUserPhoto);
		if (userPhotoData == null){
			return null;
		}
		
		// TODO if update, don't need to insert index
		if (!isExist) {
			UserPhotoIndexManager indexManager = UserPhotoIndexManager.getIndexManager(pbUserPhoto.getUsage());
			indexManager.insertIndex(userId, userPhotoData.getUserPhotoId());
		}
		
		return userPhotoData;		
	}

	

	public boolean deleteUserPhoto(String userId, String userPhotoId, int usage) {
		
		UserPhotoIndexManager indexManager = UserPhotoIndexManager.getIndexManager(usage);
		indexManager.removeId(userId, userPhotoId, false);
		
		UserPhotoManager userPhotoManager = UserPhotoManager.getInstance();
		log.info("<deleteUserPhoto> userId = "+userId+" and userphotoId = "+userPhotoId);
		boolean result = userPhotoManager.removeUserPhoto(userId,userPhotoId);
		log.info("<deleteUserPhoto> return result = "+result);
		return result;
	}

	

	public List<UserPhotoData> getUserPhotoList(String userId, int usage,
			int offset, int limit) {
		
		UserPhotoIndexManager indexManager = UserPhotoIndexManager.getIndexManager(usage);
		
		List<UserPhotoData> userPhotoDataList = indexManager.getList(userId, offset, limit, null, null, 0);
		
		return userPhotoDataList;
	}

	public List<UserPhotoData> getUserPhotoList(String userId, int usage,
			String tagList, int offset, int limit) {
		
		UserPhotoManager userPhotoManager = UserPhotoManager.getInstance();
		log.info("<getUserPhotoList> tag = "+tagList);
		String[] tags = tagList.split("\\$\\$\\$");
		List<UserPhotoData> userPhotoDataList = userPhotoManager.getList(userId, usage,tags,offset, limit);
		return userPhotoDataList;
	}
	
}
