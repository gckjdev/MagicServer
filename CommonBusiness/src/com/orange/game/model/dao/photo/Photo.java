package com.orange.game.model.dao.photo;

import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;

public class Photo extends CommonData {

	
	public Photo(DBObject obj) {
		super(obj);
	}

	public String getPhotoId(){
		return getObjectId().toString();
	}
	
	public String getURL(){
		return getString(DBConstants.F_URL);
	}
	
	public BasicDBList getTags(){
		BasicDBList list = (BasicDBList)getObject(DBConstants.F_TAG);
		return list;
	}
	
	public BasicDBList getKeywords(){
		BasicDBList list = (BasicDBList)getObject(DBConstants.F_KEYWORD);
		return list;
	}
	
}
