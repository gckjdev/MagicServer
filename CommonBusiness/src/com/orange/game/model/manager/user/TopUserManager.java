package com.orange.game.model.manager.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import com.mongodb.BasicDBObject;
import com.orange.common.db.MongoDBExecutor;
import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.service.DBService;

public class TopUserManager {

	static Logger log = Logger.getLogger(TopUserManager.class.getName());
	
	final String category;
	final String redisKey;
	final String mongoFieldName;
	
	public final static int MAX_TOP_USER_COUNT = 3000;			// only cache 3000 top users
	final static String FIELD_PREFIX_STRING = "top_";
	final static int CLEAN_CACHE_INTERVAL = 60*5;					// every 300 seconds
	
	public TopUserManager(String category){
		this.category = category.toLowerCase();
		this.redisKey = FIELD_PREFIX_STRING + this.category;
		this.mongoFieldName = FIELD_PREFIX_STRING + this.category;
		
		RedisClient.getInstance().scheduleRemoveRecordAfterZSetTop(redisKey, MAX_TOP_USER_COUNT, CLEAN_CACHE_INTERVAL);
	}
	
	public void updateUserScore(final String userId, final String opusId, final double opusScore, boolean background){
		if (background){
			DBService.getInstance().executeDBRequest(0, new Runnable() {
				
				@Override
				public void run() {
					boolean needUpdate = updateUserScoreInRedis(userId, opusScore);
					if (needUpdate){
						updateUserScoreInMongoDB(userId, opusId, opusScore);
					}
				}
			});
		}
		else{
			boolean needUpdate = updateUserScoreInRedis(userId, opusScore);
			if (needUpdate){
				updateUserScoreInMongoDB(userId, opusId, opusScore);
			}			
		}
	}
	
	public List<User> getTopUserList(final int offset, final int limit){
		
		Set<String> userSet = RedisClient.getInstance().ztop(redisKey, offset, limit);
		List<ObjectId> userIdList = new ArrayList<ObjectId>();
		for (String id : userSet){
			userIdList.add(new ObjectId(id));
		}
		
		return UserManager.getUserList(DBService.getInstance().getMongoDBClient(), userIdList);
	}

	private boolean updateUserScoreInRedis(final String userId, final double opusScore) {
		
		Object result = (Object)RedisClient.getInstance().execute(new RedisCallable<Boolean>() {
			@Override
			public Boolean call(Jedis jedis) {				
				if (redisKey == null || userId == null){
					log.error("<TopUserManager> ADD but key or member is null");
					return Boolean.FALSE;
				}
				
				Double currentScore = jedis.zscore(redisKey, userId);
				if (currentScore == null || opusScore - currentScore.doubleValue() > 0.0001){
					jedis.zadd(redisKey, opusScore, userId);
					log.info("<TopUserManager> "+userId+","+opusScore+" ADDED @"+redisKey);
					return Boolean.TRUE;
				}
				else{
					log.info("<TopUserManager> "+userId+","+opusScore+" score lower than current");
					return Boolean.FALSE;
					
				}
				
			}			
		});		
		
		if (result == null)
			return false;
		
		return ((Boolean)result).booleanValue();		
	}

	private void updateUserScoreInMongoDB(final String userId, final String opusId, final double opusScore) {
		BasicDBObject data = new BasicDBObject();
		data.put(DBConstants.F_SCORE, opusScore);
		data.put(DBConstants.F_OPUS_ID, opusId);
		data.put(DBConstants.F_MODIFY_DATE, new Date());

		BasicDBObject obj = new BasicDBObject();
		obj.put(mongoFieldName, data);
		UserManager.updateUserByDBObject(DBService.getInstance().getMongoDBClient(), userId, obj);
	}

	public int getTopUserCount() {
		return RedisClient.getInstance().ztopcount(this.redisKey);
	}
	
	
}
