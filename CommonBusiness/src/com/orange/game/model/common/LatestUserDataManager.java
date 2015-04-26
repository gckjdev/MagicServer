package com.orange.game.model.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import com.mongodb.BasicDBObject;
import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.service.DBService;


/*
 * 
 * 
 * 

 * 
 * 
 */
public abstract class LatestUserDataManager<T extends CommonData> extends CommonManager {
	
	static Logger log = Logger.getLogger(LatestUserDataManager.class.getName());
	
	final MongoGetIdListUtils<T> getIdListUtils = new MongoGetIdListUtils<T>();
	final String redisHListKey;	// 存储所有所有最新作品的list
	final String redisHMapKey;	// 用户和最新用户作品的对应map
	
	final int listTopRecordCount = 3000;
	final int listCleanCacheInterval = 60*5;
	
	final String mongoTableName;
	String mongoIdFieldName = "_id";
	
    final private Class<T> clazz;
    public T newClassInstance()
    {
        try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			log.error("<newClassInstance> catch exception "+e.toString(), e);			
			return null;
		} catch (IllegalAccessException e) {
			log.error("<newClassInstance> catch exception"+e.toString(), e);			
			return null;
		}
    }
	
	public LatestUserDataManager(String redisHListKey, String redisHMapKey, String mongoTableName, Class<T> returnDataObjectClass){
		this.redisHListKey = redisHListKey;
		this.redisHMapKey = redisHMapKey;
		this.mongoTableName = mongoTableName;
		this.clazz = returnDataObjectClass;
		
		// TODO clean data in user map		
		RedisClient.getInstance().scheduleRemoveRecordAfterListTop(redisHListKey, listTopRecordCount, listCleanCacheInterval);
	}
	
	public void insertLatestIndex(final String userId, final String userDataId, final boolean background, final boolean isOneUserOneOpus){
		if (background){
			DBService.getInstance().executeDBRequest(0, new Runnable() {
				
				@Override
				public void run() {
					insertUserDataIndex(userId, userDataId, isOneUserOneOpus);
				}
			});
		}
		else{
			insertUserDataIndex(userId, userDataId, isOneUserOneOpus);
		}
	}
	
	public List<T> getList(final int offset, final int limit, String deleteStatusFieldName, int deleteStatusValue, BasicDBObject returnMongoFields){
		
		
		Object object = RedisClient.getInstance().execute(new RedisCallable<List<String>>() {

			@Override
			public List<String> call(Jedis jedis) {
				return jedis.lrange(redisHListKey, offset, offset+limit);
			}
		});
		
		if (object == null){
			log.info("<getList> but no record fond");
			return Collections.emptyList();
		}		
		
		List<String> list = (List<String>)object;
		List<ObjectId> idList = new ArrayList<ObjectId>();
		for (String id : list){
			idList.add(new ObjectId(id));
		}
		
		log.info("<getList> total "+idList.size()+" found");
		return getIdListUtils.getList(DBService.getInstance().getMongoDBClient(), mongoTableName, mongoIdFieldName, deleteStatusFieldName, deleteStatusValue,  idList, returnMongoFields,clazz);
	}
	
	public void deleteIndex(final String userId, final String userDataId, final boolean background){
		if (background){
			DBService.getInstance().executeDBRequest(0, new Runnable() {
				@Override
				public void run() {
					deleteUserDataIndex(userId, userDataId);
				}
			});
		}
		else{
			deleteUserDataIndex(userId, userDataId);
		}
	}

	private boolean deleteUserDataIndex(final  String userId, final String userDataId) {
		Object result = (Object)RedisClient.getInstance().execute(new RedisCallable<Boolean>() {
			@Override
			public Boolean call(Jedis jedis) {				
				if (redisHListKey == null || userDataId == null){
					log.error("<deleteUserDataIndex> ADD but redisHListKey or userDataId is null");
					return Boolean.FALSE;
				}

                Pipeline p = jedis.pipelined();
//				p.multi();
				p.lrem(redisHListKey, 0, userDataId);
				p.hdel(redisHMapKey, userId);
//				Response<List<Object>> result = p.exec();
                List result = p.syncAndReturnAll();

                if (result == null){
                    log.warn("<RedisClient> pipeline exec result null "+redisHListKey+", "+redisHMapKey);
                    return Boolean.FALSE;
                }

                return Boolean.TRUE;
			}			
		});		
		
		if (result == null)
			return false;
		
		return ((Boolean)result).booleanValue();				
	}

	private boolean insertUserDataIndex(final String userId, final String userDataId, final boolean isOneUserOneOpus) {
		
		Object result = (Object)RedisClient.getInstance().execute(new RedisCallable<Boolean>() {
			@Override
			public Boolean call(Jedis jedis) {				
				if (userId == null || userDataId == null){
					log.error("<insertUserDataIndex> ADD but key or member is null");
					return Boolean.FALSE;
				}

//                Response<List<Object>> list = null;
                List list = null;
                Pipeline p = jedis.pipelined();
//                p.multi();
                if (isOneUserOneOpus){
                    String currentUserDataId = jedis.hget(redisHMapKey, userId);
                    if (currentUserDataId == null){
                        // not found
                        log.info("<insertUserDataIndex> userId("+userId+") userDataId("+userDataId+") NEW ADDED @"+redisHMapKey + ", "+redisHListKey);
                        p.hset(redisHMapKey, userId, userDataId);		// update map record
                        p.lpush(redisHListKey, userDataId);				// insert new record
                    }
                    else{
                        // found
                        log.info("<insertUserDataIndex> userId("+userId+") userDataId("+userDataId+") UPDATED @"+redisHMapKey + ", "+redisHListKey);
                        p.hset(redisHMapKey, userId, userDataId);		// update map record
                        p.lrem(redisHListKey, 0, currentUserDataId);	// remove old record
                        p.lpush(redisHListKey, userDataId);				// insert new record
                    }
                }
                else{
                    log.info("<insertUserDataIndex> just insert, userId("+userId+") userDataId("+userDataId+") UPDATED @"+redisHListKey + ", "+redisHListKey);
                    p.lpush(redisHListKey, userDataId);				// insert new record
                }
//                list = p.exec();
                list = p.syncAndReturnAll();

                if (list == null){
                    log.warn("<RedisClient> pipeline exec result null @insertUserDataIndex");
                    return Boolean.FALSE;
                }

                return Boolean.TRUE;
			}			
		});		
		
		if (result == null)
			return false;
		
		return ((Boolean)result).booleanValue();		
	}

}
