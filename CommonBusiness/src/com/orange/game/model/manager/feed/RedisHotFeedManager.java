package com.orange.game.model.manager.feed;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import redis.clients.jedis.Jedis;

import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;

public class RedisHotFeedManager implements HotFeedManagerInterface  {

	public static final Logger log = Logger.getLogger(RedisHotFeedManager.class
			.getName());
	
	final private ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(1);	
	
	volatile boolean cacheEmpty = true;
	static RedisHotFeedManager defaultManager = new RedisHotFeedManager();
	
	public static RedisHotFeedManager getInstance(){
		return defaultManager;				
	}
	
	private RedisHotFeedManager(){
//		scheduleService.scheduleAtFixedRate(command, initialDelay, period, unit)
		
		scheduleService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				// clean useless data
				RedisClient.getInstance().execute(new RedisCallable<Boolean>() {

					@Override
					public Boolean call(Jedis jedis) {				
						for (int i = 0; i < HotFeedManagerFactory.languageList.length; i++) {
							int language = HotFeedManagerFactory.languageList[i];
							String key = getRedisNamespace(language);
							Long removeCount = jedis.zremrangeByRank(key, 0, -HotFeedManagerFactory.HOT_FEED_CACHE_COUNT);
							log.info("<RedisHotFeedManager> remove cache item count="+removeCount+" for language="+language);
						}						
						return Boolean.TRUE;
					}
					
				});
				return;
			}
		}, 1, 5, TimeUnit.MINUTES);
	}
	
	@Override
	public void cacheHotFeedList() {
		// do nothing for redis since it's persist
	}

	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ObjectId> getFeedIds(final int language, final int offset, final int limit) {
		List<ObjectId> retList = (List<ObjectId>) RedisClient.getInstance().execute(new RedisCallable<List<ObjectId>>() {

			@Override
			public List<ObjectId> call(Jedis jedis) {
				
				List<ObjectId> retList = new ArrayList<ObjectId>();				
				String key = getRedisNamespace(language);
				Set<String> set = jedis.zrevrangeByScore(key, Double.MAX_VALUE, Double.MIN_VALUE, offset, limit);
				if (set != null){
					log.info("<RedisHotFeedManager> return hot feed count="+set.size());				
					for (String id : set){
						retList.add(new ObjectId(id));
					}
				}
				
				return retList;
			}
			
		});
		
		return retList;
	}

	@Override
	public void updateFeedIds(final int language, final UserAction userAction) {
		RedisClient.getInstance().execute(new RedisCallable<Boolean>() {

			@Override
			public Boolean call(Jedis jedis) {				
				String key = getRedisNamespace(language);
				log.info("<RedisHotFeedManager> add action "+userAction.getActionId()+" hot="+userAction.getHot());				
				jedis.zadd(key, userAction.getHot(), userAction.getActionId());				
				return Boolean.TRUE;
			}
			
		});
	}

	@Override
	public void deleteAction(final String actionId, final int language) {
		final int lang = language;
		final String id = actionId;
		RedisClient.getInstance().execute(new RedisCallable<Boolean>() {

			@Override
			public Boolean call(Jedis jedis) {
				log.info("<RedisHotFeedManager> remove action "+actionId);				
				jedis.zrem(getRedisNamespace(lang), id);
				return Boolean.TRUE;
			}
			
		});
	}

	@Override
	public void updateAllFeeds(final int language,  final List<UserAction> actionList) {
		RedisClient.getInstance().execute(new RedisCallable<Boolean>() {

			@Override
			public Boolean call(Jedis jedis) {
				
				String key = getRedisNamespace(language);
				for (UserAction userAction : actionList){
					jedis.zadd(key, userAction.getHot(), userAction.getActionId());
					log.info("<RedisHotFeedManager> add action "+userAction.getActionId()+" hot="+userAction.getHot());
				}
				
				return Boolean.TRUE;
			}
			
		});
	}
	
	private String getRedisNamespace(int language){
		if (language == 2){
			return DBConstants.REDIS_NS_HOT_FEED_EN;
		}
		else{
			return DBConstants.REDIS_NS_HOT_FEED_CN;			
		}
	}

	@Override
	public boolean isCacheEmpty() {
		Boolean result = (Boolean)RedisClient.getInstance().execute(new RedisCallable<Boolean>() {			
			@Override
			public Boolean call(Jedis jedis) {
				
				if (!cacheEmpty)
					return Boolean.FALSE;
				
				Long cnRecordCount = jedis.zcount(DBConstants.REDIS_NS_HOT_FEED_CN, Double.MIN_VALUE, Double.MAX_VALUE);
				Long enRecordCount = jedis.zcount(DBConstants.REDIS_NS_HOT_FEED_EN, Double.MIN_VALUE, Double.MAX_VALUE);
				
				log.info("<RedisHotFeedManager> CN count="+cnRecordCount+", EN count="+enRecordCount);
				
				if (cnRecordCount.longValue() == 0 || enRecordCount.longValue() == 0){
					cacheEmpty = true;
					return Boolean.TRUE;					
				}
				else{
					cacheEmpty = false;
					return Boolean.FALSE;
				}
			}
		});
				
		return result.booleanValue();
	}

	@Override
	public void loadDBCachedData() {
		// do nothing for redis here since no cache in app memory
	}

}
