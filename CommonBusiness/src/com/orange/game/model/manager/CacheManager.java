package com.orange.game.model.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.feed.HotFeedManager;

public class CacheManager extends CommonManager {

	public static int CACHE_TYPE_HOT_FEED_LIST = 1;

	public static void cacheData(MongoDBClient mongoClient, int type,
			Object value) {
		DBObject query = new BasicDBObject(DBConstants.F_TYPE, type);
		DBObject set = new BasicDBObject(DBConstants.F_VALUE, value);
		set.put(DBConstants.F_TYPE, type);
		set.put(DBConstants.F_CREATE_DATE, new Date());
		DBObject update = new BasicDBObject("$set", set);
		mongoClient.upsertAll(DBConstants.T_CACHE, query, update);
	}

	public static Object getCacheData(MongoDBClient mongoClient, int type) {
		DBObject query = new BasicDBObject(DBConstants.F_TYPE, type);
		DBObject object = mongoClient.findOne(DBConstants.T_CACHE, query);
		if (object != null) {
			Object obj = object.get(DBConstants.F_VALUE);
			return obj;
		}
		return null;
	}

	public static void cacheHotFeedList(MongoDBClient mongoClient,
			HashMap<Integer, List<UserAction>> map) {
		if (map != null) {
			try {
				synchronized (map) {
					DBObject value = new BasicDBObject();
					for (Integer key : map.keySet()) {
						List<UserAction> list = map.get(key);
						BasicDBList dbList = getDBList(list);
						if (dbList != null) {
							value.put(key.toString(), dbList);
						}
					}
					cacheData(mongoClient, CACHE_TYPE_HOT_FEED_LIST, value);
				}
			} catch (Exception e) {
				log.error("<cacheHotFeedList> Exception:"+e.toString(), e);
			}
		}
	}

	public static HashMap<Integer, List<UserAction>> getCachedHotFeedList(
			MongoDBClient mongoClient) {
		
		HashMap<Integer, List<UserAction>> retMap = new HashMap<Integer, List<UserAction>>();
		DBObject object = (DBObject) getCacheData(mongoClient,
				CACHE_TYPE_HOT_FEED_LIST);
		if (object != null) {
			try {
				int[] languageList = HotFeedManager.languageList;
				
				for (int lang : languageList) {
					String key = Integer.toString(lang);
					BasicDBList dbList = (BasicDBList) object.get(key);
//					log.info("key = " + key + ", value = " + dbList);
					if (dbList != null) {
						List<UserAction> actions = (List<UserAction>) getCommonDataList(
								dbList, UserAction.class);
//						log.info("actions = " + actions);
						if (!actions.isEmpty()) {
							retMap.put(lang, actions);
						}
					}
				}
				return retMap;
			} catch (Exception e) {
				log.error("<getCachedHotFeedList> Exception:");
				e.printStackTrace();
			}
		}
		return retMap;
	}

	private static List<? extends CommonData> getCommonDataList(
			BasicDBList dbList, Class<? extends CommonData> commonDataClass) {
		if (dbList != null) {
			List<CommonData> list = new ArrayList<CommonData>();
			for (Object object : dbList) {
				if (object instanceof DBObject) {
					try {
						CommonData data = commonDataClass.newInstance();
						data.setDbObject((DBObject) object);
//						log.info("Create CommonData = " + data);
						list.add(data);
					} catch (Exception e) {
						log.info("Create CommonData Instance Exception");
						e.printStackTrace();
					}
				} else {
					log.warn(object + "is not instance of DBObject.");
				}
			}
			return list;
		}
		return Collections.emptyList();

	}

	private static BasicDBList getDBList(List<? extends CommonData> list) {
		if (list != null && !list.isEmpty()) {
			BasicDBList dbList = new BasicDBList();
			for (CommonData data : list) {
				dbList.add(data.getDbObject());
			}
			return dbList;
		}
		return null;
	}

}
