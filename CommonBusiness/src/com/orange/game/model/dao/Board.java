package com.orange.game.model.dao;

import java.util.List;
import org.bson.types.ObjectId;
import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;

public class Board extends CommonData {

	// BoardTypes
	public static int BoardTypeDefault = 1;
	public static int BoardTypeWeb = 2;
	public static int BoardTypeImage = 3;

	// BoardStatus
	public static int BoardStatusRun = 1;// 进行中
	public static int BoardStatusStop = 2;

	// WebType
	public static int WebTypeLocal = 1;
	public static int WebTypeRemote = 2;

	public static int BoardDeviceTypeIPhone = 1;
	public static int BoardDeviceTypeIPad = 2;


	public static int AdPlatformLm = 1;
	public static int AdPlatformAder = 2;
	public static int AdPlatformMango = 3;

	// common attributes

	public String getBoardId() {
		return getObjectId().toString();
	}

	public Board(DBObject dbObject) {
		super(dbObject);
	}

	public void setBoardId(ObjectId oid) {
		dbObject.put(DBConstants.F_OBJECT_ID, oid);
	}

	public int getIndex() {
		return getInt(DBConstants.F_INDEX);
	}

	public int getType() {
		return getInt(DBConstants.F_TYPE);
	}

	public String getVersion() {
		return getString(DBConstants.F_VERSION);
	}

	public int getStatus() {
		return getInt(DBConstants.F_STATUS);
	}

	public void setDeviceType(List<Integer> deviceTypes) {
		put(DBConstants.F_DEVICE_TYPE, deviceTypes);
	}

	public void setGameIds(List<String> gameIds) {
		put(DBConstants.F_GAMEID, gameIds);
	}

	public void setIndex(int index) {
		dbObject.put(DBConstants.F_INDEX, index);
	}

	public void setType(int type) {
		dbObject.put(DBConstants.F_TYPE, type);
	}

	public void setVersion(String version) {
		dbObject.put(DBConstants.F_VERSION, version);
	}

	public void setStatus(int status) {
		dbObject.put(DBConstants.F_STATUS, status);
	}

	// ad attributes

	public int getAdPlatform() {
		return getInt(DBConstants.PLATFORM);
	}

	public String getAdPublishId() {
		return getString(DBConstants.PUBLISID);
	}

	public void setAdPlatform(int platform) {
		dbObject.put(DBConstants.PLATFORM, platform);
	}

	public void setAdPublishId(String publishId) {
		dbObject.put(DBConstants.PUBLISID, publishId);
	}

	// web attributes

	public int getWebType() {
		return getInt(DBConstants.F_WEB_TYPE);
	}

	public String getLocalUrl() {
		return getString(DBConstants.F_LOCAL_URL);
	}

	public String getRemoteUr() {
		return getString(DBConstants.F_REMOTE_URL);
	}

	public void setWebType(int webType) {
		put(DBConstants.F_WEB_TYPE, webType);
	}

	public void setLocalUrl(String localUrl) {
		put(DBConstants.F_LOCAL_URL, localUrl);
	}

	public void setRemoteUr(String remoteUr) {
		put(DBConstants.F_REMOTE_URL, remoteUr);
	}

	// image attrubutes

	public String getImageUrl() {
		return getString(DBConstants.F_IMAGE_URL);
	}

	public String getAdImageUrl() {
		return getString(DBConstants.F_AD_IMAGE_URL);
	}

	public void setAdImageUrl(String imageUrl) {
		put(DBConstants.F_AD_IMAGE_URL, imageUrl);
	}

	public void setImageUrl(String imageUrl) {
		put(DBConstants.F_IMAGE_URL, imageUrl);
	}

	//international.
	public String getCNImageUrl() {
		return getString(DBConstants.F_CN_IMAGE_URL);
	}

	public String getCNAdImageUrl() {
		return getString(DBConstants.F_CN_AD_IMAGE_URL);
	}

	public void setCNAdImageUrl(String imageUrl) {
		put(DBConstants.F_CN_AD_IMAGE_URL, imageUrl);
	}

	public void setCNImageUrl(String imageUrl) {
		put(DBConstants.F_CN_IMAGE_URL, imageUrl);
	}

	
	public String getClickUrl() {
		return getString(DBConstants.F_CLICK_URL);
	}

	public void setClickUrl(String clickUrl) {
		put(DBConstants.F_CLICK_URL, clickUrl);
	}

	public int getReward() {
		return getInt(DBConstants.F_REWARD);
	}

}
