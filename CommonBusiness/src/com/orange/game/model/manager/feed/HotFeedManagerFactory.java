package com.orange.game.model.manager.feed;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.CacheManager;
import com.orange.game.model.manager.OpusManager;

public class HotFeedManagerFactory {
	
	public static final Logger log = Logger.getLogger(HotFeedManagerFactory.class.getName());
	
	public static final int HOT_FEED_CACHE_COUNT = 300;
	public static final int[] languageList = { 1, 2 };	// 1 : Chinese, 2 : English
	
	protected static MongoDBClient mongoClient = null;
	
	public static void setMongoDBClient(MongoDBClient client){
		mongoClient = client;
	}
	
	public static HotFeedManagerInterface getHotFeedManager(){
//		HotFeedManagerInterface model = HotFeedManager.getInstance();			// old implementation
		HotFeedManagerInterface manager = RedisHotFeedManager.getInstance(); 	// redis implementation
		manager.loadDBCachedData();
		if (manager.isCacheEmpty()){
			constructMapFromOriginalData(manager);
		}
		
		return manager;
	}
	
	public static void constructMapFromOriginalData(HotFeedManagerInterface manager) {
		for (int i = 0; i < languageList.length; i++) {
			int language = languageList[i];
			constructFeedIds(language, manager);
		}
		log.info("<HotFeedManagerFactory>: finish init and load data from database.");
	}
	
	// should update once. find the top 100
	private static void constructFeedIds(int language, HotFeedManagerInterface manager) {

		HashMap<Integer, List<UserAction>> oldMongoMap = CacheManager.getCachedHotFeedList(mongoClient);		
		
		List<UserAction> actionList = oldMongoMap.get(language);
		if (actionList == null || actionList.size() == 0){
			log.info("<HotFeedManagerFactory> start to construct feed id list. language = " + language);		
			actionList = OpusManager.getSimpleHotFeedList(mongoClient, 0, HOT_FEED_CACHE_COUNT, language);			
		}
		else{
			log.info("<HotFeedManagerFactory> load old cache map from mongo DB, record count="+actionList.size());					
		}
				
		if (actionList != null) {
			Collections.sort(actionList);
			manager.updateAllFeeds(language, actionList);
			log.info("<HotFeedManagerFactory> construct feed id successful, language = "+ language + ", queue = " + actionList);
		} else {
			log.error("<HotFeedManagerFactory> construct feed id fail, language = "+ language);
		}
	}	
	
}
