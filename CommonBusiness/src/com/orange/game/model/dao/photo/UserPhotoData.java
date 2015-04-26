package com.orange.game.model.dao.photo;

import java.util.List;

import com.mongodb.DBObject;
import com.orange.common.utils.DateUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.model.PhotoProtos.PBPhotoUsage;


public class UserPhotoData extends CommonData {
	
	public UserPhotoData(DBObject obj) {
		super(obj);
	}
	
	public UserPhotoData() {
	}

	public String getUserPhotoId() {
		return this.getObjectId().toString();
	}

	public static boolean isValidUsage(int usage) {
		if (usage > PBPhotoUsage.BEGIN_VALUE && usage < PBPhotoUsage.END_VALUE)
			return true;
		return false;
	}

	public int getCreateDate() {
		return DateUtil.dateToInt(getDate(DBConstants.F_MODIFY_DATE));
	}

	public String getUserId() {
		return getString(DBConstants.F_UID);
	}

	public String getUrl() {
		return getString(DBConstants.F_URL);
	}

	public int getUsage() {
		return getInt(DBConstants.F_USAGE);
	}

	public String getName() {
		
		return getString(DBConstants.F_NAME);
	}

	public List<String> getTagList() {
		return getStringList(DBConstants.F_TAG);
	}

	public String getPhotoId() {
		return getString(DBConstants.F_PHOTO_ID);
	}

	public float getHeight() {
		return getFloat(DBConstants.F_HEIGHT);
	}

	public float getWidth() {
		return getFloat(DBConstants.F_WIDTH);
	}

}
