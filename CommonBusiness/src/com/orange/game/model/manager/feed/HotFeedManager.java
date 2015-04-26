package com.orange.game.model.manager.feed;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;


import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.CacheManager;
import com.orange.game.model.manager.OpusManager;

public class HotFeedManager extends FeedManager implements HotFeedManagerInterface {
	private static HotFeedManager hotFeedManager = null;

	public static final Logger log = Logger.getLogger(HotFeedManager.class
			.getName());

	private static HashMap<Integer, List<UserAction>> hotFeedsCacheMap = new HashMap<Integer, List<UserAction>>();
	private static final int hotFeedCacheCount = 120;

	public static final int[] languageList = { 1, 2 };
	// English

	private static final String lock = "LOCK";

	private static final long TIMER_INTERVAL = 10 * 60 * 1000; // update the

	// cached data
	// per ten
	// minutes

	private HotFeedManager() {
		super();
	}

	public static int getHotFeedCachecount() {
		return hotFeedCacheCount;
	}

	public static HotFeedManager getInstance() {
		synchronized (lock) {
			if (hotFeedManager == null) {
				log
						.info("<HotFeedManager>: start to init and load data from database.");
				hotFeedManager = new HotFeedManager();
				// load data from db cached
				hotFeedManager.loadDBCachedData();
				if (!hotFeedManager.isMapConstructed()) {
					log.info("Fail to load Cached Hot Feed List");
					log.info("Start to Construct Hot Feed List");

					// load data from db original data
					hotFeedManager.constructMapFromOriginalData();

					// user a timer to cache data
					hotFeedManager.startScheduleCacheTask(true);
				} else {
					log.info("Success to load Cached Hot Feed List");
					hotFeedManager.startScheduleCacheTask(false);
				}
			}
			return hotFeedManager;
		}
	}

	public static boolean isEnableScheduleTask = getIsEnableScheduleTask();
	
	public static boolean getIsEnableScheduleTask(){
		String str = System.getProperty("feed.disable_hot_feed_scheduler");
		if (StringUtil.isEmpty(str)){
			return true; 
		}
		else{
			log.info("DISABLE HOT FEED SCHEDULE TASK.");
			return false;
		}
	}
	
	private void startScheduleCacheTask(boolean now) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				log.info("TimerTask clocks: cache hot feed list");
				if (isEnableScheduleTask){
					hotFeedManager.cacheHotFeedList();
				}
				else{
					log.info("TimerTask clocks: cache hot feed list, but it's disabled");
				}
			}
		};
		Timer timer = new Timer();
		long delay = now ? 0 : TIMER_INTERVAL;
		timer.schedule(task, delay, TIMER_INTERVAL);
	}

	@Override	
	public void cacheHotFeedList() {
		log.info("Start to Cache Hot Feed List");
		CacheManager.cacheHotFeedList(mongoClient, hotFeedsCacheMap);
	}

	private void constructMapFromOriginalData() {
		for (int i = 0; i < languageList.length; i++) {
			int language = languageList[i];
			constructFeedIds(language);
		}
		log.info("<HotFeedManager>: finish init and load data from database.");
	}

	private boolean isMapConstructed() {
		if (hotFeedsCacheMap == null || hotFeedsCacheMap.isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	public void loadDBCachedData() {
		log.info("Start to load Cached Hot Feed List");
		hotFeedsCacheMap = CacheManager.getCachedHotFeedList(mongoClient);
		log.info("Finish to load Cached Hot Feed List");
		if (hotFeedsCacheMap == null) {
			hotFeedsCacheMap = new HashMap<Integer, List<UserAction>>();
		}
	}

	private List<UserAction> getHotQueue(int language) {
		List<UserAction> pq = hotFeedsCacheMap.get(language);
		if (pq == null) {
			pq = new ArrayList<UserAction>();
			hotFeedsCacheMap.put(language, pq);
		}
		return pq;
	}

	@Override	
	public List<ObjectId> getFeedIds(int language, int offset, int limit) {
		List<UserAction> pq = getHotQueue(language);
		if (pq == null || pq.size() == 0) {
			return null;
		}
		int endIndex = pq.size() - offset;
		int startIndex = endIndex - limit;
		if (startIndex < 0) {
			startIndex = 0;
		}
		log.info("<HotFeedManager>: get sub list, start index = " + startIndex
				+ ", endIndex = " + endIndex + ", list size = " + pq.size());

		if (startIndex > pq.size() || startIndex >= endIndex
				|| endIndex > pq.size()) {
			return null;
		}
		List<UserAction> subList = pq.subList(startIndex, endIndex);
		if (subList == null || subList.size() == 0) {
			return null;
		}

		List<ObjectId> idList = new ArrayList<ObjectId>();
		for (UserAction action : subList) {
			idList.add(action.getObjectId());
		}
		// notify();
		return idList;

	}

	// should update once. find the top 100
	private void constructFeedIds(int language) {

		// MongoDBClient mongoClient = mongoDBClientHolder.get();

		log
				.info("<HotFeedManager> start to construct feed id list. language = "
						+ language);
		List<UserAction> actionList = OpusManager.getSimpleHotFeedList(
				mongoClient, 0, hotFeedCacheCount, language);
		if (actionList != null) {
			Collections.sort(actionList);
			hotFeedsCacheMap.put(language, actionList);
			log
					.info("<HotFeedManager> construct feed id successful, language = "
							+ language + ", queue = " + actionList);
		} else {
			log
					.info("warnning <HotFeedManager> construct feed id fail, language = "
							+ language);
		}
	}

	private int indexForUserActionId(List<UserAction> pq, String actionId) {
		for (int i = 0; i < pq.size(); i++) {
			if (pq.get(i).getActionId().equalsIgnoreCase(actionId)) {
				return i;
			}
		}
		return -1;
	}

	private void printPQ(List<UserAction> pq) {
		System.out.println("\n\n*****************\n pq = ");
		for (int i = 0; i < pq.size(); i++) {
			System.out.println("action id = " + pq.get(i).getActionId()
					+ ", hot = " + pq.get(i).getHot());
		}
		System.out.println("\n*******************\n\n");
	}

	private void insertUserAction(List<UserAction> pq, UserAction aAction) {
		if (pq == null) {
			return;
		}
		synchronized (pq) {
			// copy an simple action with id and hot
			UserAction action = simpleAction(aAction);
			if (pq.size() < hotFeedCacheCount
					|| pq.get(0).getHot() < action.getHot()) {
				log
						.info("<HotFeedManager> insert action into hot list. actionId = "
								+ action.getActionId()
								+ ", action hot = "
								+ action.getHot());

				int index = indexForUserActionId(pq, action.getActionId());

				// if find the same action, remove all the all action
				if (index >= 0) {
					pq.remove(index);
				}
				index = Collections.binarySearch(pq, action);
				if (index <= 0) {
					index = -index - 1;
				}
				pq.add(index, action);
				if (pq.size() > hotFeedCacheCount) {
					pq.remove(0);
				}
			}
			// printPQ(pq);
		}
		// notify();
	}

	private UserAction simpleAction(UserAction aAction) {
		if (aAction != null) {
			UserAction action = new UserAction();
			action.setActionId(aAction.getObjectId());
			action.setHot(aAction.getHot());
			return action;
		}
		return null;
	}

	@Override
	public void updateFeedIds(int language, UserAction action) {
		List<UserAction> pq = getHotQueue(language);
		insertUserAction(pq, action);
	}

	@Override
	public void deleteAction(String actionId, int language) {
		List<UserAction> pq = getHotQueue(language);
		if (pq == null) {
			return;
		}
		synchronized (pq) {
			UserAction action = null;
			for (int i = 0; i < pq.size(); i++) {
				if (pq.get(i).getActionId().equalsIgnoreCase(actionId)) {
					action = pq.get(i);
					break;
				}
			}
			if (action != null) {
				pq.remove(action);
			}
		}
	}

	@Override
	public void updateAllFeeds(int language, List<UserAction> actionList) {
		hotFeedsCacheMap.put(language, actionList);		
	}

	@Override
	public boolean isCacheEmpty() {
		return (isMapConstructed() == false);
	}

}
