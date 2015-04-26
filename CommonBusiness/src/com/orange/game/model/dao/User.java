package com.orange.game.model.dao;

import java.util.*;

import com.orange.common.elasticsearch.ESORMable;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.MapUtil;
import com.orange.common.utils.RandomUtil;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.group.Group;
import com.orange.game.model.manager.verification.UserVerificationManager;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.model.GameBasicProtos;
import net.sf.json.JSONObject;


import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.app.AbstractApp;
import com.orange.game.model.manager.utils.ImageUploadManager;
import com.orange.network.game.protocol.model.GameBasicProtos.PBOpenInfoType;
import org.bson.types.ObjectId;
import org.eclipse.jetty.util.ConcurrentHashSet;

public class User extends CommonData implements ESORMable, MapUtil.MakeMapable<ObjectId, User>, ProtoBufCoding<GameBasicProtos.PBGameUser>{

    public static final String NULL_DEVICE_ID = "00000000-0000-0000-0000-000000000000";
    private static final int RANDOM_PASSWORD_LENGTH = 6;
    private static final int DEFAULT_SHAKE_TIMES = 10;
    private Group group;

    public static boolean isValidDevice(String deviceId){
        if (StringUtil.isEmpty(deviceId))
            return false;

        if (deviceId.equalsIgnoreCase(NULL_DEVICE_ID))
            return false;

        return true;
    }


    public User(DBObject dbObject) {
		super(dbObject);
		// TODO Auto-generated constructor stub
	}

	public User() {
		super();
	}
	
	public String getUserId() {
		return this.getObjectId().toString();
	}

	public BasicDBList getItemList(){
		BasicDBList list = (BasicDBList) this.getDbObject().get(
				DBConstants.F_ITEMS);
		return list;
	}
	
	private int safeGetInt(BasicDBObject obj, String key){
		if (obj.containsField(key)){
			return obj.getInt(key);
		}
		else{
			return 0;
		}
	}
	
	public List<Item> getItems() {

		BasicDBList list = (BasicDBList) this.getDbObject().get(
				DBConstants.F_ITEMS);
		if (list == null) {
			return Collections.emptyList();
		}
		Iterator<Object> iter = list.iterator();
		List<Item> itemList = new ArrayList<Item>();
        Set<Integer> itemAdded = new HashSet<Integer>();
		while (iter.hasNext()) {
			BasicDBObject obj = (BasicDBObject) iter.next();
			if (obj != null) {
				int type = obj.getInt(DBConstants.F_ITEM_TYPE);
				int amount = obj.getInt(DBConstants.F_ITEM_AMOUNT);
				
				int createDate = safeGetInt(obj, DBConstants.F_CREATE_DATE); // obj.getInt();
				int modifyDate = safeGetInt(obj, DBConstants.F_MODIFY_DATE); //obj.getInt(DBConstants.F_MODIFY_DATE);
				int expireDate = safeGetInt(obj, DBConstants.F_EXPIRE_DATE); // obj.getInt(DBConstants.F_EXPIRE_DATE);
				
				Item item = new Item(type, amount, createDate, modifyDate, expireDate);
				itemList.add(item);

                itemAdded.add(type);
			}
		}

        if (isVip()){
            // add all pends
            int pens[] = {
                    DBConstants.C_ITEM_TYPE_PEN_QUILL,
                    DBConstants.C_ITEM_TYPE_PEN_WATER,
                    DBConstants.C_ITEM_TYPE_PEN_ICE,
                    DBConstants.C_ITEM_TYPE_PEN_PEN,
                    DBConstants.C_ITEM_TYPE_FUN_PEN1,
                    DBConstants.C_ITEM_TYPE_FUN_PEN2,
                    DBConstants.C_ITEM_TYPE_FUN_PEN3,
                    DBConstants.C_ITEM_TYPE_FUN_PEN4,
                    DBConstants.C_ITEM_TYPE_FUN_PEN5,
            };

            for (int i=0; i<pens.length; i++){
                if (!itemAdded.contains(i)){
                    Item item = new Item(i, 1, 0, 0, 0);
                    itemList.add(item);
                }
            }
        }

		return itemList;
	}

	public int getItemAmountByType(int itemType) {
		BasicDBList list = (BasicDBList) this.getDbObject().get(
				DBConstants.F_ITEMS);
		if (list == null) {
			return 0;
		}
		Iterator<Object> iter = list.iterator();
		while (iter.hasNext()) {
			BasicDBObject obj = (BasicDBObject) iter.next();
			if (obj != null) {
				int type = obj.getInt(DBConstants.F_ITEM_TYPE);
				if (type == itemType) {
					int amount = obj.getInt(DBConstants.F_ITEM_AMOUNT);
					return amount;
				}
			}
		}
		return 0;
	}

	public BasicDBObject getLevelObjectByGameId(String gameId){
		String field = AbstractApp.userLevelField(gameId);
		Object obj = getObject(field);
		if (obj == null){
			return null;
		}
		else {
			return (BasicDBObject)obj;
		}
	}

	
	public BasicDBObject getLevelObjectByAppId(String appId){
		String field = AbstractApp.userLevelFieldByAppId(appId);
		Object obj = getObject(field);
		if (obj == null){
			return null;
		}
		else {
			return (BasicDBObject)obj;
		}
	}
	
	public int getLevelByAppId(String appId){
		
		BasicDBObject obj = getLevelObjectByAppId(appId);
		if (obj == null)
			return 0;
		
		return obj.getInt(DBConstants.F_LEVEL); 
		
		// old implement 
		// return getLevelByAppId(appId, false);
	}
	
	public int getLevelByGameId(String gameId){
		
		BasicDBObject obj = getLevelObjectByGameId(gameId);
		if (obj == null)
			return 0;
		
		return obj.getInt(DBConstants.F_LEVEL); 
		
		// old implement 
		// return getLevelByAppId(appId, false);
	}

	
	@Deprecated
	private int getLevelByAppId(String id, boolean isGameId) {
		Object list = this.getDbObject().get(DBConstants.F_LEVEL_INFO);
		if (list == null) {
			return 1;
		}
		Iterator<Object> iter = ((BasicDBList) list).iterator();
		while (iter.hasNext()) {
			BasicDBObject obj = (BasicDBObject) iter.next();
			if (obj != null) {
				String gameId = id;
				if (!isGameId){
					gameId = App.getGameIdByAppId(id);
				}
				String type = obj.getString(DBConstants.F_CREATE_SOURCE_ID);
				if (type != null && !type.isEmpty() && type.equals(gameId)) {
					int level = obj.getInt(DBConstants.F_LEVEL);
					return level;
				}
			}
		}
		return 1;
	}

	public long getExpByAppId(String appId){

		BasicDBObject obj = getLevelObjectByAppId(appId);
		if (obj == null)
			return 0;
		
		return obj.getInt(DBConstants.F_EXP_NEW); 
		
//		return getExpByAppId(id, false);
	}
	
	public long getExpByGameId(String gameId) {
		
		BasicDBObject obj = getLevelObjectByGameId(gameId);
		if (obj == null)
			return 0;
		
		return obj.getInt(DBConstants.F_EXP_NEW); 
	}
	
	@Deprecated	
	private long getExpByAppId(String id, boolean isGameId) {
		Object list = this.getDbObject().get(DBConstants.F_LEVEL_INFO);
		if (list == null) {
			return 0;
		}
		Iterator<Object> iter = ((BasicDBList) list).iterator();
		while (iter.hasNext()) {
			BasicDBObject obj = (BasicDBObject) iter.next();
			if (obj != null) {
				String gameId = id;
				if (!isGameId){
					gameId = App.getGameIdByAppId(id);
				}
				String type = obj.getString(DBConstants.F_CREATE_SOURCE_ID);
				if (type != null && !type.isEmpty() && type.equals(gameId)) {
					long exp = obj.getLong(DBConstants.F_EXP);
					return exp;
				}
			}
		}
		return 0;
	}

//	private JSONObject getLevelInfoByAppId(String appId) {
//		JSONObject object = new JSONObject();
//		Object list = this.getDbObject().get(DBConstants.F_LEVEL_INFO);
//		if (list == null) {
//			return object;
//		}
//		Iterator<Object> iter = ((BasicDBList) list).iterator();
//		while (iter.hasNext()) {
//			BasicDBObject obj = (BasicDBObject) iter.next();
//			if (obj != null) {
//				String gameId = App.getGameIdByAppId(appId);
//				String type = obj.getString(DBConstants.F_CREATE_SOURCE_ID);
//				if (type != null && !type.isEmpty() && type.equals(gameId)) {
//					long exp = obj.getLong(DBConstants.F_EXP);
//					int level = obj.getInt(DBConstants.F_LEVEL);
////					object.put(DBConstant.PARA_EXP, exp);
////					object.put(ServiceConstant.PARA_LEVEL, level);
//					object.put(DBConstants.F_EXP, exp);
//					object.put(DBConstants.F_LEVEL, level);
//					
//				}
//			}
//		}
//		return object;
//	}
	
	public JSONObject getLevelInfoByGameId(String gameId) {
		
		JSONObject object = new JSONObject();
		BasicDBObject dbObject = getLevelObjectByGameId(gameId);
		if (dbObject == null){
			return object;
		}
		
		long exp = dbObject.getLong(DBConstants.F_EXP_NEW);
		int level = dbObject.getInt(DBConstants.F_LEVEL);
		object.put(DBConstants.F_EXP, exp);
		object.put(DBConstants.F_LEVEL, level);
		
		return object;
		
		/*
		
		JSONObject object = new JSONObject();
		Object list = this.getDbObject().get(DBConstants.F_LEVEL_INFO);
		if (list == null) {
			return object;
		}
		Iterator<Object> iter = ((BasicDBList) list).iterator();
		while (iter.hasNext()) {
			BasicDBObject obj = (BasicDBObject) iter.next();
			if (obj != null) {
				String type = obj.getString(DBConstants.F_CREATE_SOURCE_ID);
				if (type != null && !type.isEmpty() && type.equals(gameId)) {
					long exp = obj.getLong(DBConstants.F_EXP);
					int level = obj.getInt(DBConstants.F_LEVEL);
					object.put(DBConstants.F_EXP, exp);
					object.put(DBConstants.F_LEVEL, level);
				}
			}
		}
		return object;
		*/
	}

	private Object getLevelInfo() {
		return this.getDbObject().get(DBConstants.F_LEVEL_INFO);
	}

	public int getBalance() {
		return this.getInt(DBConstants.F_ACCOUNT_BALANCE);
	}

	public void setBalance(int balance) {
		this.put(DBConstants.F_ACCOUNT_BALANCE, balance);
	}

	public int getIngotBalance() {
		return this.getInt(DBConstants.F_ACCOUNT_INGOT_BALANCE);
	}

	public void setIngotBalance(int balance) {
		this.put(DBConstants.F_ACCOUNT_INGOT_BALANCE, balance);
	}
	
	public String getAppId() {
		return this.getString(DBConstants.F_APPID);
	}

	public void setAppId(String appId) {
		this.put(DBConstants.F_APPID, appId);
	}

	public String getSourceId() {
		return this.getString(DBConstants.F_CREATE_SOURCE_ID);
	}

	public void setSourceId(String sourceId) {
		this.put(DBConstants.F_CREATE_SOURCE_ID, sourceId);
	}

	public void setEmail(String email) {
		this.put(DBConstants.F_EMAIL, email);
	}

	public String getEmail() {
		return this.getString(DBConstants.F_EMAIL);
	}

	public String getPassword() {
		return this.getString(DBConstants.F_PASSWORD);
	}

	public void setPassword(String newPassword) {
		this.put(DBConstants.F_PASSWORD, newPassword);
	}

	public String getVerificationCode(int type, String email) {
        String field = UserVerificationManager.getInstance().getVerifyCodeFieldName(type);
        return getString(field);

	}

	public void setVerificationCode(String verifyCode) {
		this.getDbObject().put(DBConstants.F_VERIFYCODE, verifyCode);
	}

	public Date getCreateDate() {
		return this.getDate(DBConstants.F_CREATE_DATE);
	}

	public void setCreateDate(Date createDate) {
		this.getDbObject().put(DBConstants.F_CREATE_DATE, createDate);
	}

	public int getStatus() {
		return this.getInt(DBConstants.F_STATUS);
	}

	public String getGender() {
		return this.getString(DBConstants.F_GENDER);
	}

	public boolean isMale() {
		String genderString = this.getString(DBConstants.F_GENDER);
		if (genderString == null)
			return false;
		else {
			return genderString.equals("m");
		}
	}

	public String getLocation() {
		return this.getString(DBConstants.F_LOCATION);
	}

	public String getFacebookId() {
		return this.getString(DBConstants.F_FACEBOOKID);
	}

	public void setStatus(int status) {
		this.getDbObject().put(DBConstants.F_STATUS, status);
	}

    public List<String> getAllDeviceId(){
        List<String> list = new ArrayList<String>();
        String deviceId = getDeviceId();
        if (!StringUtil.isEmpty(deviceId)){
            list.add(deviceId);
        }

        list.addAll(getStringList(DBConstants.F_DEVICEID_LIST));
        return list;
    }

	public String getDeviceToken() {
		String deviceToken = this.getString(DBConstants.F_DEVICETOKEN);
        if (StringUtil.isEmpty(deviceToken)){
            List<String> deviceTokens = getStringList(DBConstants.F_DEVICETOKEN_LIST);
            if (deviceTokens != null && deviceTokens.size() > 0){
                return deviceTokens.get(0);
            }
            else{
                // search in device info
                List<String> deviceList = getAllDeviceId();
                if (deviceList.size() == 0){
                    return null;
                }

                BasicDBList deviceInfoList = getList(DBConstants.F_DEVICES);
                if (deviceInfoList != null){
                    for (Object obj : deviceInfoList){
                        BasicDBObject deviceInfo = (BasicDBObject)obj;
                        if (StringUtil.isEmpty(deviceInfo.getString(DBConstants.F_DEVICETOKEN)) == false){
                            if (deviceList.contains(deviceInfo.getString(DBConstants.F_DEVICEID))){
                                return deviceInfo.getString(DBConstants.F_DEVICETOKEN);
                            }
                        }
                    }
                }

                return null;
            }
        }
        else{
            return deviceToken;
        }
	}

	public void setDeviceToken(String deviceToken) {
		this.getDbObject().put(DBConstants.F_DEVICETOKEN, deviceToken);
	}

	public void setAvatar(String avatarURL) {
		this.getDbObject().put(DBConstants.F_AVATAR, avatarURL);
	}

	public String getAvatar() {
		String url = this.getString(DBConstants.F_AVATAR);
		return getTranslatedAvatar(url);
	}

	private static final String OLD_IMAGE_KEY = "http://img.you100.me:8080/upload/";
    private static final String OLD_IMAGE_KEY1 = "http://vm-192-168-13-89.hd01.sdcloud.cn:8080/upload/";

	private static final String NEW_IMAGE_KEY = ImageUploadManager.getUserAvatarManager().getRemoteDir(); //"58.215.160.100";

	public static String getTranslatedAvatar(String avatarURL) {
		String url = avatarURL;
		if (url == null)
			return null;

        if (url.indexOf("http://") == -1){
            // relative URL
            return ImageUploadManager.getUserAvatarManager().getRemoteURL(url);
        }

		url = url.replace(OLD_IMAGE_KEY, NEW_IMAGE_KEY);
        return url.replace(OLD_IMAGE_KEY1, NEW_IMAGE_KEY);
	}

	public void setNickName(String nickName) {
		this.put(DBConstants.F_NICKNAME, nickName);
	}

	public String getNickName() {
		return this.getString(DBConstants.F_NICKNAME);
	}

	public String getSinaNickName() {
		return this.getString(DBConstants.F_SINA_NICKNAME);
	}

	public String getSinaRefreshToken() {
		return this.getString(DBConstants.F_SINA_REFRESH_TOKEN);
	}
	
	public int getSinaExpireDate() {
		Date date = this.getDate(DBConstants.F_SINA_EXPIRE_DATE);
		if (date != null)
			return (int)(date.getTime()/1000);
		else
			return 0;
	}		
	
	public String getQQNickName() {
		return this.getString(DBConstants.F_QQ_NICKNAME);
	}

	public String getQQAccessToken() {
		return this.getString(DBConstants.F_QQ_ACCESS_TOKEN);
	}

	public String getQQAccessTokenSecret() {
		return this.getString(DBConstants.F_QQ_ACCESS_TOKEN_SECRET);
	}
	
	public String getQQOpenId() {
		return this.getString(DBConstants.F_QQ_OPEN_ID);
	}

	public String getQQRefreshToken() {
		return this.getString(DBConstants.F_QQ_REFRESH_TOKEN);
	}
	
	public int getQQExpireDate() {
		Date date = this.getDate(DBConstants.F_QQ_EXPIRE_DATE);
		if (date != null)
			return (int)(date.getTime()/1000);
		else
			return 0;
	}	
	
	public String getFacebookAccessToken() {
		return this.getString(DBConstants.F_FACEBOOK_ACCESS_TOKEN);
	}
	
	public int getFacebookExpireDate() {
		Date date = this.getDate(DBConstants.F_FACEBOOK_EXPIRE_DATE);
		if (date != null)
			return (int)(date.getTime()/1000);
		else
			return 0;
	}
	public String getSinaAccessToken() {
		return this.getString(DBConstants.F_SINA_ACCESS_TOKEN);
	}

	public String getSinaAccessTokenSecret() {
		return this.getString(DBConstants.F_SINA_ACCESS_TOKEN_SECRET);
	}

	public String getSinaID() {
		return this.getString(DBConstants.F_SINAID);
	}

	public String getQQID() {
		return this.getString(DBConstants.F_QQID);
	}

	public void setDeviceId(String deviceId) {
        if (isValidDevice(deviceId)){
		    this.put(DBConstants.F_DEVICEID, deviceId);
        }
	}
	
	public void addItem(int itemType, int itemAmount) {
		BasicDBList list = (BasicDBList) this.getDbObject().get(
				DBConstants.F_ITEMS);
		
		int currentTime = (int)(System.currentTimeMillis()/1000);
		
		if (list == null) {
			list = new BasicDBList();
			BasicDBObject itemObject = new BasicDBObject();
			itemObject.put(DBConstants.F_ITEM_TYPE, itemType);
			itemObject.put(DBConstants.F_ITEM_AMOUNT, itemAmount);
			itemObject.put(DBConstants.F_CREATE_DATE, currentTime);
			itemObject.put(DBConstants.F_MODIFY_DATE, currentTime);
			list.add(itemObject);
			this.getDbObject().put(DBConstants.F_ITEMS, list);
			return;
		}

		boolean found = false;
		for (Object obj : list) {
			BasicDBObject item = (BasicDBObject) obj;
			if (item.getInt(DBConstants.F_ITEM_TYPE) == itemType) {	
				int currentAmount = item.getInt(DBConstants.F_ITEM_AMOUNT);
				int newAmount = (currentAmount + itemAmount);
				if (newAmount < 0)
					newAmount = 0;
				item.put(DBConstants.F_ITEM_AMOUNT, newAmount);
				item.put(DBConstants.F_MODIFY_DATE, currentTime);
				found = true;
				break;
			}
		}

		if (!found) {
			BasicDBObject itemObject = new BasicDBObject();
			itemObject.put(DBConstants.F_ITEM_TYPE, itemType);
			itemObject.put(DBConstants.F_ITEM_AMOUNT, itemAmount);
			if (itemAmount < 0){
				itemAmount = 0;
			}
			itemObject.put(DBConstants.F_CREATE_DATE, currentTime);
			itemObject.put(DBConstants.F_MODIFY_DATE, currentTime);
			list.add(itemObject);
		}

		this.getDbObject().put(DBConstants.F_ITEMS, list);
	}
	

	public void createOrUpdateItem(int itemType, int itemAmount) {
		BasicDBList list = (BasicDBList) this.getDbObject().get(
				DBConstants.F_ITEMS);
		
		int currentTime = (int)(System.currentTimeMillis()/1000);
		
		if (list == null) {
			list = new BasicDBList();
			BasicDBObject itemObject = new BasicDBObject();
			itemObject.put(DBConstants.F_ITEM_TYPE, itemType);
			itemObject.put(DBConstants.F_ITEM_AMOUNT, itemAmount);
			itemObject.put(DBConstants.F_CREATE_DATE, currentTime);
			itemObject.put(DBConstants.F_MODIFY_DATE, currentTime);
			list.add(itemObject);
			this.getDbObject().put(DBConstants.F_ITEMS, list);
			return;
		}

		boolean found = false;
		for (Object obj : list) {
			BasicDBObject item = (BasicDBObject) obj;
			if (item.getInt(DBConstants.F_ITEM_TYPE) == itemType) {				
				item.put(DBConstants.F_ITEM_AMOUNT, itemAmount);
				item.put(DBConstants.F_MODIFY_DATE, currentTime);
				found = true;
				break;
			}
		}

		if (!found) {
			BasicDBObject itemObject = new BasicDBObject();
			itemObject.put(DBConstants.F_ITEM_TYPE, itemType);
			itemObject.put(DBConstants.F_ITEM_AMOUNT, itemAmount);
			itemObject.put(DBConstants.F_CREATE_DATE, currentTime);
			itemObject.put(DBConstants.F_MODIFY_DATE, currentTime);
			list.add(itemObject);
		}

		this.getDbObject().put(DBConstants.F_ITEMS, list);
	}

	public void setGender(String gender) {
		this.dbObject.put(DBConstants.F_GENDER, gender);
	}
	

	private DBObject createOrUpdateLevelInfo(String appId, long exp, int level) {
		DBObject returnDbObject = null;
		BasicDBList list = (BasicDBList) this.getDbObject().get(
				DBConstants.F_LEVEL_INFO);
		if (list == null) {
			list = new BasicDBList();
			BasicDBObject levelObject = new BasicDBObject();
			levelObject.put(DBConstants.F_CREATE_SOURCE_ID, appId);
			levelObject.put(DBConstants.F_EXP, exp);
			levelObject.put(DBConstants.F_LEVEL, level);
			list.add(levelObject);
			this.getDbObject().put(DBConstants.F_LEVEL_INFO, list);
			returnDbObject = levelObject;
		}

		boolean found = false;
		for (Object obj : list) {
			BasicDBObject levelObject = (BasicDBObject) obj;
			if (levelObject.getString(DBConstants.F_CREATE_SOURCE_ID).equals(
					appId)) {
				levelObject.put(DBConstants.F_EXP, exp);
				levelObject.put(DBConstants.F_LEVEL, level);
				found = true;
				returnDbObject = levelObject;
				break;
			}
		}

		if (!found) {
			BasicDBObject levelObject = new BasicDBObject();
			levelObject.put(DBConstants.F_EXP, exp);
			levelObject.put(DBConstants.F_LEVEL, level);
			levelObject.put(DBConstants.F_CREATE_SOURCE_ID, appId);
			list.add(levelObject);
			returnDbObject = levelObject;
		}
		this.getDbObject().put(DBConstants.F_LEVEL_INFO, list);
		return returnDbObject;
	}

	// public List<String> getFollowUids() {
	// BasicDBList followList = (BasicDBList)
	// dbObject.get(DBConstants.F_FOLLOWS);
	// if (followList == null || followList.size() == 0) {
	// return null;
	// }
	// List<String>followUids = new ArrayList<String>();
	// for (Object object : followList) {
	// DBObject followUser = (DBObject) object;
	// String fid = (String) followUser.get(DBConstants.F_FRIENDID);
	// if (fid != null && fid.length() !=0) {
	// followUids.add(fid);
	// }
	// }
	// return followUids;
	// }

	// public List<ObjectId> getFollowUidList() {
	// BasicDBList followList = (BasicDBList)
	// dbObject.get(DBConstants.F_FOLLOWS);
	// if (followList == null || followList.size() == 0) {
	// return null;
	// }
	// List<ObjectId>followUids = new ArrayList<ObjectId>();
	// for (Object object : followList) {
	// DBObject followUser = (DBObject) object;
	// String fid = (String) followUser.get(DBConstants.F_FRIENDID);
	// if (fid != null && fid.length() !=0) {
	// followUids.add(new ObjectId(fid));
	// }
	// }
	// return followUids;
	// }

	// public List<ObjectId> getFanUidList() {
	// BasicDBList fanList = (BasicDBList) dbObject.get(DBConstants.F_FANS);
	// if (fanList == null || fanList.size() == 0) {
	// return null;
	// }
	// List<ObjectId>fanUids = new ArrayList<ObjectId>();
	// for (Object object : fanList) {
	// DBObject followUser = (DBObject) object;
	// String fid = (String) followUser.get(DBConstants.F_FRIENDID);
	// if (fid != null && fid.length() !=0) {
	// fanUids.add(new ObjectId(fid));
	// }
	// }
	// return fanUids;
	// }

	public void setLocation(String location) {
		this.put(DBConstants.F_LOCATION, location);

	}

	public int getGuessBalance() {
		return getInt(DBConstants.F_GUESS_BALANCE);
	}

	public void setVersion(String version) {
		this.put(DBConstants.F_VERSION, version);

	}

	public int getAwardExp() {
		return getInt(DBConstants.F_AWARD_EXP);
	}

	public int getNewFanCount() {
		return getInt(DBConstants.F_NEW_FAN_COUNT);
	}

	public int getNewBBSActionCount() {
		return getInt(DBConstants.F_NEW_BBSACTION_COUNT);
	}

	public boolean hasAppId(String appId) {
		BasicDBList list = (BasicDBList) dbObject.get(DBConstants.F_APPID_LIST);
		if (list != null) {
			return list.contains(appId);
		}
		return false;
	}

	public List<String> getAppIdList() {
		return getStringList(DBConstants.F_APPID_LIST);
	}
	
	public void addAppId(String appId) {
		BasicDBList list = (BasicDBList) dbObject.get(DBConstants.F_APPID_LIST);
		if (list == null) {
			list = new BasicDBList();
		}

		if (list.contains(appId))
			return;

		// add appId and save it
		list.add(appId);
		dbObject.put(DBConstants.F_APPID_LIST, list);
	}

	public String getDeviceId() {
		return getString(DBConstants.F_DEVICEID);
	}

	public String getVersion() {
		return getString(DBConstants.F_VERSION);
	}

	public int getDrawToMeCount(AbstractXiaoji xiaoji) {
        if (xiaoji == null){
            return 0;
        }

		return getInt(xiaoji.getDrawToMeField()); //DBConstants.F_DRAWTOME_COUNT);
	}

	public void setRelation(int relation) {
		dbObject.put(DBConstants.F_RELATION, relation);
	}

	public int getRelation() {
		return getInt(DBConstants.F_RELATION);
	}

	public boolean hasEnoughIngotBalance(int value) {
		return getIngotBalance() >= value;
	}

	public boolean hasEnoughBalance(int value) {
		return getBalance() >= value;
	}

	public boolean getBoolGender() {
		return isMale();
	}

	public String getFacebookNickName() {
		return "";
	}

	public String getFacebookRefreshToken() {
		return null;
	}

	public String getBirthday() {
		return getString(DBConstants.F_BIRTHDAY);
	}

	public int getZodiac() {
		return getInt(DBConstants.F_ZODIAC);
	}

	public int getGuessWordLanguage() {
		return getInt(DBConstants.F_GUESS_WORD_LANGUAGE);
	}

	public String getBackgroundRemoteURL() {
		String localPath = getString(DBConstants.F_BACKGROUND);
		if (!StringUtil.isEmpty(localPath)){
			return ImageUploadManager.getUserBackgroundManager().getRemoteURL(localPath);
		}
		else{
			return null;
		}
	}

	public static String genderFromBool(boolean gender) {
		if (gender)
			return "m";
		else
			return "f";
	}

	public String getSignature() {
		return getString(DBConstants.F_SIGNATURE);
	}

	public String getBlood() {
		return getString(DBConstants.F_BLOOD);
	}

	public String getCountryCode() {
		return getString(DBConstants.F_COUNTRYCODE);
	}

	public String getLanguageCode() {
		return getString(DBConstants.F_LANGUAGE);
	}
	
	
	public PBOpenInfoType getOpenInfoType() {
		int value = getInt(DBConstants.F_OPEN_INFO_TYPE);
		return PBOpenInfoType.valueOf(value);
	}

	int fanCount = 0;
	int followCount = 0;
	
	public void setFanCount(long fanCount) {
		this.fanCount = (int)fanCount;
	}

	public void setFollowCount(long followCount) {
		this.followCount = (int)followCount;
	}

	public int getFanCount() {
		return fanCount;
	}

	public int getFollowCount() {
		return followCount;
	}

	public boolean isChina() {
		String country = this.getCountryCode();
		String language = this.getLanguageCode();
		if (country != null && country.equalsIgnoreCase("CN")){
			return true;
		}
		
		if (language != null && 
				( language.equalsIgnoreCase("zh_CN") || language.equalsIgnoreCase("zh_Hans"))){
			return true;
		}
		
		return false;
	}

	public static BasicDBObject createUserLevelObject(String gameId, int level, long exp) {
		String field = AbstractApp.userLevelField(gameId);
		BasicDBObject obj = new BasicDBObject();
		obj.put(DBConstants.F_LEVEL, level);
		obj.put(DBConstants.F_EXP_NEW, exp);
		obj.put(DBConstants.F_MODIFY_DATE, new Date());		
		return new BasicDBObject(field, obj);
	}


    public static boolean boolGender(String gender) {
        if (gender == null){
            return false;
        }

        return gender.equals("m");
    }

    public int getSingRecordLimit() {
        return getInt(DBConstants.F_SING_RECORD_LIMIT);
    }

    public boolean hasDeviceId(String deviceId) {
        BasicDBList deviceList = (BasicDBList)dbObject.get(DBConstants.F_DEVICEID_LIST);
        if (deviceList == null)
            return false;

        if (deviceList.contains(deviceId))
            return true;

        String userDeviceId = getDeviceId();
        if (StringUtil.isEmpty(userDeviceId)){
            return false;
        }

        return userDeviceId.equalsIgnoreCase(deviceId);
    }

    public void initDeviceInfo(String deviceId, String deviceToken, String deviceModel, String deviceOs, int deviceType) {

        BasicDBList deviceIdList = new BasicDBList();
        if (User.isValidDevice(deviceId)){
            deviceIdList.add(deviceId);
        }
        put(DBConstants.F_DEVICEID_LIST, deviceIdList);


        BasicDBList deviceTokenList = new BasicDBList();
        if (!StringUtil.isEmpty(deviceToken)){
            deviceTokenList.add(deviceToken);
        }
        put(DBConstants.F_DEVICETOKEN_LIST, deviceTokenList);

        BasicDBList deviceList = new BasicDBList();
        if (!StringUtil.isEmpty(deviceId)){
            BasicDBObject deviceObj = new BasicDBObject();
            deviceObj.put(DBConstants.F_DEVICETOKEN, deviceToken);
            deviceObj.put(DBConstants.F_DEVICEID, deviceId);
            deviceObj.put(DBConstants.F_DEVICEMODEL, deviceModel);
            deviceObj.put(DBConstants.F_DEVICE_TYPE, deviceType);
            deviceObj.put(DBConstants.F_DEVICEOS, deviceOs);

            deviceList.add(deviceObj);

        }
        put(DBConstants.F_DEVICES, deviceList);

    }

    public int getFeatureOpus() {
        return getInt(DBConstants.F_FEATURE_OPUS);
    }

    public boolean checkPassword(String passwordToCompare) {

        String password = getPassword();
        if (StringUtil.isEmpty(password)){
            return false;
        }
        return password.equalsIgnoreCase(passwordToCompare);
    }

    public String createPassword() {

        return RandomUtil.randomNumberString(RANDOM_PASSWORD_LENGTH);
    }

    static final String PASSWORD_KEY = "PASSWORD_KEY_DRAW_DSAQC";     // must align with client settings

    public String encryptPassword(String password) {
        if (password == null)
            return null;

        return StringUtil.md5base64encode(password + PASSWORD_KEY);
    }

    public String getXiaojiNumber() {
        return getString(DBConstants.F_XIAOJI_NUMBER);
    }

    public String getFriendMemo() {
        return getString(DBConstants.F_MEMO);
    }

    public GameBasicProtos.PBGameUser toPBUser() {

        try {
            GameBasicProtos.PBGameUser.Builder builder = GameBasicProtos.PBGameUser.newBuilder();
            builder.setUserId(this.getUserId());

            String nickName = this.getNickName();
            if (nickName != null)
                builder.setNickName(nickName);
            else {
                builder.setNickName("");
            }

            String avatar = this.getAvatar();
            if (avatar != null)
                builder.setAvatar(avatar);

            boolean gender = this.getBoolGender();
            builder.setGender(gender);

            String location = this.getLocation();
            if (location != null)
                builder.setLocation(location);

            builder.setVip(this.getFinalVip());

//            int level = user.getLevelByAppId(appId);
//            builder.setLevel(level);
//            builder.setUserLevel(level);
//
//            long exp = user.getExpByAppId(appId);
//            builder.setExperience(exp);
//
//            String email = user.getEmail();
//            if (email != null)
//                builder.setEmail(email);
//
//            String password = user.getPassword();
//            if (password != null)
//                builder.setPassword(password);
//
//            builder.setCoinBalance(user.getBalance());
//            builder.setIngotBalance(user.getIngotBalance());
//            builder.setFeatureOpus(user.getFeatureOpus());
//
//            // sns user - SINA
//            if (!StringUtil.isEmpty(user.getSinaID()) && !StringUtil.isEmpty(user.getSinaNickName())){
//                String snsId = user.getSinaID();
//                String snsNick = user.getSinaNickName();
//                String accessToken = user.getSinaAccessToken();
//                String refreshToken = user.getSinaRefreshToken();
//                int expireTime = user.getSinaExpireDate();
//
//                GameBasicProtos.PBSNSUser.Builder snsBuilder = GameBasicProtos.PBSNSUser.newBuilder();
//                snsBuilder.setType(ServiceConstant.REGISTER_TYPE_SINA);
//                snsBuilder.setUserId(snsId);
//                snsBuilder.setNickName(snsNick);
//
//                if (accessToken != null){
//                    snsBuilder.setAccessToken(accessToken);
//                }
//
//                if (refreshToken != null){
//                    snsBuilder.setRefreshToken(refreshToken);
//                }
//
//                snsBuilder.setExpireTime(expireTime);
//                builder.addSnsUsers(snsBuilder.build());
//            }
//
//            // sns user - QQ
//            if (!StringUtil.isEmpty(user.getQQID()) && !StringUtil.isEmpty(user.getQQNickName())){
//                String snsId = user.getQQID();
//                String snsNick = user.getQQNickName();
//                String accessToken = user.getQQAccessToken();
//                String refreshToken = user.getQQRefreshToken();
//                int expireTime = user.getQQExpireDate();
//                String qqOpenId = user.getQQOpenId();
//
//                GameBasicProtos.PBSNSUser.Builder snsBuilder = GameBasicProtos.PBSNSUser.newBuilder();
//                snsBuilder.setType(ServiceConstant.REGISTER_TYPE_QQ);
//                snsBuilder.setUserId(snsId);
//                snsBuilder.setNickName(snsNick);
//
//                if (accessToken != null){
//                    snsBuilder.setAccessToken(accessToken);
//                }
//
//                if (refreshToken != null){
//                    snsBuilder.setRefreshToken(refreshToken);
//                }
//
//                if (qqOpenId != null){
//                    snsBuilder.setQqOpenId(qqOpenId);
//                }
//
//                snsBuilder.setExpireTime(expireTime);
//                builder.addSnsUsers(snsBuilder.build());
//            }
//
//            if (!StringUtil.isEmpty(user.getFacebookId())){
//                String snsId = user.getFacebookId();
//                String snsNick = user.getFacebookNickName();
//                String accessToken = user.getFacebookAccessToken();
//                String refreshToken = user.getFacebookRefreshToken();
//                int expireTime = user.getFacebookExpireDate();
//
//                GameBasicProtos.PBSNSUser.Builder snsBuilder = GameBasicProtos.PBSNSUser.newBuilder();
//                snsBuilder.setType(ServiceConstant.REGISTER_TYPE_FACEBOOK);
//                snsBuilder.setUserId(snsId);
//                snsBuilder.setNickName(snsNick);
//
//                if (accessToken != null){
//                    snsBuilder.setAccessToken(accessToken);
//                }
//
//                if (refreshToken != null){
//                    snsBuilder.setRefreshToken(refreshToken);
//                }
//
//                snsBuilder.setExpireTime(expireTime);
//                builder.addSnsUsers(snsBuilder.build());
//            }
//
//            // user item
////            setItemIntoUserBuilder(builder, user);
//
//            String birthday = user.getBirthday();
//            if (birthday != null)
//                builder.setBirthday(birthday);
//
//            builder.setZodiac(user.getZodiac());
//            builder.setGuessWordLanguage(user.getGuessWordLanguage());
//
//            String deviceToken = user.getDeviceToken();
//            if (deviceToken != null)
//                builder.setDeviceToken(deviceToken);
//
//            String backgroundURL = user.getBackgroundRemoteURL();
//            if (backgroundURL != null)
//                builder.setBackgroundURL(backgroundURL);

            String signature = this.getSignature();
            if (signature != null)
                builder.setSignature(signature);

//            String blood = user.getBlood();
//            if (blood != null)
//                builder.setBloodGroup(blood);
//
//            int singRecordLimit = user.getSingRecordLimit();
//            if (singRecordLimit != 0){
//                builder.setSingRecordLimit(singRecordLimit);
//            }
//
//            GameBasicProtos.PBOpenInfoType openInfoType = user.getOpenInfoType();
//            builder.setOpenInfoType(openInfoType);
//
//            builder.setFanCount(user.getFanCount());
//            builder.setFollowCount(user.getFollowCount());

            GameBasicProtos.PBGameUser pbUser = builder.build();
            return pbUser;

        } catch (Exception e) {
            log.error("<userToPB> catch exception"+e.toString(), e);
            return null;
        }
    }

    public boolean isVerified(String email, int type) {

        String field = UserVerificationManager.getInstance().getVerifyStatusFieldName(type);
        if (field == null)
            return false;

        int value = getInt(field);
        return UserVerificationManager.getInstance().isVerified(value);
    }

    public boolean hasValidVerifyCode(String email, int type) {

        String field = UserVerificationManager.getInstance().getVerifyCodeFieldName(type);
        if (field == null)
            return false;

        String code = getString(field);
        if (StringUtil.isEmpty(code)){
            return false;
        }

        return true;
    }

    public int verfiyCode(String codeToCompare, int type, String email) {

        String field = UserVerificationManager.getInstance().getVerifyCodeFieldName(type);
        if (field == null)
            return ErrorCode.ERROR_SEND_TYPE_NOT_SUPPORT;

        String code = getString(field);
        if (StringUtil.isEmpty(code)){
            return ErrorCode.ERROR_USER_VERIFYCODE_NULL;
        }

        if (code.equalsIgnoreCase(codeToCompare))
            return 0;
        else
            return ErrorCode.ERROR_USER_VERIFYCODE_INVALID;

    }

    public int getEmailVerifyStatus() {
        String field = UserVerificationManager.getInstance().getVerifyStatusFieldName(UserVerificationManager.TYPE_EMAIL);
        return getInt(field);
    }

    public boolean getCanShakeNumber() {

        if (getXiaojiNumber() != null){
            return false;
        }

        Boolean value = (Boolean)dbObject.get(DBConstants.F_SHAKE_XIAOJI);
        if (value == null){
            // by default, it's true, if field not exists in user data
            return true;
        }

        return value.booleanValue();
    }

    public int getShakeNumberTimes(){

        if (getCanShakeNumber() == false){
            return 0;
        }

        Object obj = dbObject.get(DBConstants.F_SHAKE_NUMBER_TIMES);
        if (obj == null){
            // if no value, then use default, by default is 10
            return DEFAULT_SHAKE_TIMES;
        }
        else{
            return getInt(DBConstants.F_SHAKE_NUMBER_TIMES);
        }
    }

    public int getTakeCoins() {
        return getInt(DBConstants.F_TAKE_COINS);
    }

    public boolean hasSetTakeCoinsForItem() {

        return getBoolean(DBConstants.F_CALCULATE_TAKE_COINS);
    }

    public List<String> getBlockDeviceIds(){
        List<String> list = getStringList(DBConstants.F_BLOCK_DEVICES);
        return list;
    }

    public boolean isInBlockDeviceList(String deviceId) {

        List<String> list = getStringList(DBConstants.F_BLOCK_DEVICES);
        if (list == null || list.size() == 0){
            return false;
        }

        if (list.indexOf(deviceId) == -1){
            return false;
        }
        else{
            log.info("detect device "+deviceId+" in user "+getUserId()+"'s block device id list!");
            return true;
        }
    }

    public double getPopScore() {
        return getDouble(DBConstants.F_POP_SCORE);
    }

    @Override
    public Map<String, Object> getESORM() {


        if (dbObject != null && getUserId() != null){
            Map<String, Object> userBean = new HashMap<String, Object>();

            String nick_name = StringUtil.getEmptyStringWhenNull(getNickName());
            String email = StringUtil.getEmptyStringWhenNull(getEmail());
            String sina_nick = StringUtil.getEmptyStringWhenNull(getSinaNickName());
            String qq_nick = StringUtil.getEmptyStringWhenNull(getQQNickName());
            String facebook_id = StringUtil.getEmptyStringWhenNull(getFacebookNickName());
            String signature = StringUtil.getEmptyStringWhenNull(getSignature());
            String user_id = StringUtil.getEmptyStringWhenNull(getUserId());
            String sina_id = StringUtil.getEmptyStringWhenNull(getSinaID());
            String qq_id = StringUtil.getEmptyStringWhenNull(getQQID());
            String xiaoji = getXiaojiNumber();

            userBean.put(DBConstants.ES_NICK_NAME, StringUtil.getEmptyStringWhenNull(getNickName()));
            userBean.put(DBConstants.ES_EMAIL, email);
            userBean.put(DBConstants.ES_SINA_NICK, sina_nick);
            userBean.put(DBConstants.ES_QQ_NICK,qq_nick);
            userBean.put(DBConstants.ES_SINA_ID, sina_id);
            userBean.put(DBConstants.ES_QQ_ID,qq_id);
            userBean.put(DBConstants.ES_FACEBOOK_ID,facebook_id);
            userBean.put(DBConstants.ES_SIGNATURE, signature);
            userBean.put(DBConstants.ES_USER_ID, user_id);
            userBean.put(DBConstants.ES_XIAOJI_NUMBER, xiaoji);

            return userBean;
        }
        return  null;
    }

    @Override
    public String getESIndexType() {
        return DBConstants.ES_INDEX_TYPE_USER;
    }

    @Override
    public String getID() {
        return getStringObjectId();
    }

    @Override
    public List<String> fieldsForIndex() {
        return null;
    }


    @Override
    public String getESIndexName() {
        return DBConstants.ES_INDEX_NAME;
    }

    @Override
    public boolean canBeIndexed()
    {
    	return true;    	
    }
    
    @Override
    public boolean hasFieldForSearch() {

	return hasFieldForSearch(dbObject);

	/*
        if (dbObject == null) return false;

        return (dbObject.containsField(DBConstants.F_NICKNAME)      ||
                dbObject.containsField(DBConstants.F_QQ_NICKNAME)   ||
                dbObject.containsField(DBConstants.F_SINA_NICKNAME) ||
                dbObject.containsField(DBConstants.F_QQID)          ||
                dbObject.containsField(DBConstants.F_SINAID)        ||
                dbObject.containsField(DBConstants.F_EMAIL)         ||
                dbObject.containsField(DBConstants.F_FACEBOOKID)    ||
                dbObject.containsField(DBConstants.F_XIAOJI_NUMBER));
    	*/
    }

    public boolean hasFieldForSearch(DBObject dbObject) {

        if (dbObject == null) return false;

        return (dbObject.containsField(DBConstants.F_NICKNAME)      ||
                dbObject.containsField(DBConstants.F_QQ_NICKNAME)   ||
                dbObject.containsField(DBConstants.F_SINA_NICKNAME) ||
                dbObject.containsField(DBConstants.F_QQID)          ||
                dbObject.containsField(DBConstants.F_SINAID)        ||
                dbObject.containsField(DBConstants.F_EMAIL)         ||
                dbObject.containsField(DBConstants.F_FACEBOOKID)    ||
                dbObject.containsField(DBConstants.F_XIAOJI_NUMBER));
    }


    @Override
    public String toString() {
        return "User{" +
                "userId=" + getUserId() +
                ", nick=" + getNickName() +
                '}';
    }
    
    public static String getSNSCredentialKey(int loginIdType) {

        switch (loginIdType){
            case DBConstants.LOGINID_QQ:
                return DBConstants.F_CREDENTIAL_QQ_WEIBO;
            case DBConstants.LOGINID_QQSPACE:
                return DBConstants.F_CREDENTIAL_QQ_SPACE;
            case DBConstants.LOGINID_SINA:
                return DBConstants.F_CREDENTIAL_SINA_WEIBO;
            case DBConstants.LOGINID_FACEBOOK:
                return DBConstants.F_CREDENTIAL_FACEBOOK;
            default:
                return null;
        }
    }

    public static String getSNSIdKey(int loginIdType) {

        switch (loginIdType){
            case DBConstants.LOGINID_QQSPACE:
                return DBConstants.F_QQSPACE_ID;
            case DBConstants.LOGINID_QQ:
                return DBConstants.F_QQID;
            case DBConstants.LOGINID_SINA:
                return DBConstants.F_SINAID;
            case DBConstants.LOGINID_FACEBOOK:
                return DBConstants.F_FACEBOOKID;
            default:
                return null;
        }
    }

    static final ConcurrentHashSet<String> LOGIN_KEYS = new ConcurrentHashSet<String>();

    public static Set<String> getLoginKeys(){

        if (LOGIN_KEYS.size() > 0){
            return LOGIN_KEYS;
        }

        synchronized (LOGIN_KEYS){
            for (int i=DBConstants.LOGINID_START; i<=DBConstants.LOGINID_END; i++){
                String key = getSNSCredentialKey(i);
                if (key != null){
                    LOGIN_KEYS.add(key);
                }
            }
        }

        return LOGIN_KEYS;
    }

    public static boolean registerAfterAugust(String userId) {

//        if (StringUtil.isEmpty(userId)){
//            return true;
//        }
//
//        ObjectId objId = new ObjectId(userId);
//        Date date = DateUtil.dateFromString("20130818000000");
//        if (objId.getTime() >= date.getTime()){
//            return true;
//        }
//        else{
//            return false;
//        }

        return DateUtil.idAfterDate(userId, "20130818000000");

    }

    @Override
    public ObjectId getKey() {
        return getObjectId();
    }

    @Override
    public User getValue() {
        return this;
    }

    @Override
    public GameBasicProtos.PBGameUser toProtoBufModel() {
        return toPBUser();
    }

    @Override
    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {
        builder.addUserList(toProtoBufModel());
    }

    public Collection<ObjectId> getGroupIds() {
        Collection<ObjectId> groupIds = (Collection<ObjectId>)getObject(DBConstants.F_GROUPS);
        if (groupIds == null){
            return Collections.emptyList();
        }
        return groupIds;
    }

    public ObjectId getFirstGroupId()
    {
        List<ObjectId> groupIds = (List)this.getGroupIds();
        if (groupIds.isEmpty()){
            return null;
        }
        return groupIds.get(0);
    }

    public void setGroup(Group group){
        this.group = group;
    }

    public Group getGroup(){
        return group;
    }

    public boolean isVip() {

        int vip = getInt(DBConstants.F_VIP);
        if (vip == 0){
            return false;
        }

        Date expireDate = getDate(DBConstants.F_VIP_EXPIRE_DATE);
        if (expireDate == null){
            return false;
        }

        if (expireDate.before(new Date())){
            return false;
        }

        return true;
    }

    public Date getVipExpireDate() {
        Date expireDate = getDate(DBConstants.F_VIP_EXPIRE_DATE);
        return expireDate;
    }

    public Date getVipLastPayDate() {
        Date date = getDate(DBConstants.F_VIP_LAST_PAY_DATE);
        return date;
    }

    public int getFinalVip() {
        if (!isVip()){
            return 0;
        }

        return getVip();
    }

    public int getVip() {
        return getInt(DBConstants.F_VIP);
    }

    public int getIntVipLastPayDate() {
        return getIntDate(DBConstants.F_VIP_LAST_PAY_DATE);
    }

    public int getIntVipExpireDate() {
        return getIntDate(DBConstants.F_VIP_EXPIRE_DATE);
    }

    public boolean hasChargeVipCoin() {

        Date date = getDate(DBConstants.F_VIP_MONTHLY_CHARGE);
        if (date == null){
            return false;
        }

        int chargeMonth = DateUtil.getMonth(date);
        int currentMonth = DateUtil.getMonth(new Date());
        if (chargeMonth == currentMonth){
            return true;
        }

        return false;
    }

    public List<String> getAwardAppIdList() {
        return getStringList(DBConstants.F_AWARD_APP_LIST);
    }

    public boolean hasAwardApp(String awardAppId) {
        if (StringUtil.isEmpty(awardAppId)){
            return true;
        }

        List<String> apps = getAwardAppIdList();
        return apps.contains(awardAppId);
    }

    public boolean hasDeviceInfo(String deviceId) {

        BasicDBList list = getList(DBConstants.F_DEVICES);
        if (list == null){
            return false;
        }

        for (Object obj : list){
            String did = ((BasicDBObject)obj).getString(DBConstants.F_DEVICEID);
            if (did != null && did.equalsIgnoreCase(deviceId)){
                return true;
            }
        }

        return false;
    }

    public List<BasicDBObject> getNoDuplicateDeviceInfo() {

        BasicDBList list = getList(DBConstants.F_DEVICES);
        if (list == null){
            return Collections.emptyList();
        }

        Set<String> deviceids = new HashSet<String>();
        List<BasicDBObject> retList = new ArrayList<BasicDBObject>();

        for (Object obj : list){
            String did = ((BasicDBObject)obj).getString(DBConstants.F_DEVICEID);
            if (did == null){
                continue;
            }

            if (deviceids.contains(did)){
                continue;
            }

            retList.add((BasicDBObject)obj);
            deviceids.add(did);
        }

        return retList;
    }

    public List<String> getDeviceTokens() {
        return getStringList(DBConstants.F_DEVICETOKEN_LIST);
    }

    public List<String> getOffGroups() {
        return getStringList(DBConstants.F_OFF_GROUPS);
    }

    public List<String> getPermissions(){
        return getStringList(DBConstants.F_PERMISSIONS);
    }

    public boolean canBlackUser() {

        List<String> permissions = getStringList(DBConstants.F_PERMISSIONS);
        if (permissions.contains(DBConstants.C_PERMISSION_BLACK_USER)){
            return true;
        }

        return false;

    }

    public String getDeviceModel() {
        return getString(DBConstants.F_DEVICEMODEL);
    }

    public void setDeviceModel(String deviceModel) {
        put(DBConstants.F_DEVICEMODEL, deviceModel);
    }

    public void setXiaoji(String number) {
        put(DBConstants.F_XIAOJI_NUMBER, number);
    }

    public String getResetPassword() {
        return getString(DBConstants.F_RESET_PASSWORD);
    }

    public boolean needCreateNewPassword(){
        Date now = new Date();
        Date resetDate = getDate(DBConstants.F_RESET_PASSWORD_DATE);
        if (resetDate == null){
            return true;
        }

        long leadTime = (now.getTime() - resetDate.getTime());
        // valid in 24 hour
        long hour = 24;
        if (leadTime > 1000*60*60*hour){
            return true;
        }
        else{
            return false;
        }
    }
}


