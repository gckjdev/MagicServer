package com.orange.game.model.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import redis.clients.jedis.Jedis;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.service.DBService;

public abstract class CommonZSetIndexManager<T extends CommonData> {

	protected static Logger log = Logger.getLogger(CommonZSetIndexManager.class.getName());

    protected static final int NO_LIMIT = -1;
	
	final MongoGetIdListUtils<T> getIdListUtils = new MongoGetIdListUtils<T>();
	final String redisKey;
	protected int zsetTopRecordCount = NO_LIMIT;
	final int zsetCleanCacheInterval = 60*5;
	
	final String mongoTableName;
	String mongoIdFieldName = "_id";
	
    final private Class<T> clazz;
    final private RedisClient redisClient = RedisClient.getInstance();

    public void setMongoIdFieldName(String mongoIdFieldName) {
        this.mongoIdFieldName = mongoIdFieldName;
    }

    public String getRedisKey()
    {
        return redisKey;
    }

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

    public CommonZSetIndexManager(String redisKey, String mongoTableName, int topCount, Class<T> returnDataObjectClass){
        this.redisKey = redisKey;
        this.mongoTableName = mongoTableName;
        this.clazz = returnDataObjectClass;
        this.zsetTopRecordCount = topCount;

        if (topCount > 0){
            RedisClient.getInstance().scheduleRemoveRecordAfterZSetTop(redisKey, zsetTopRecordCount, zsetCleanCacheInterval);
        }
    }
	
	public CommonZSetIndexManager(String redisKey, String mongoTableName, Class<T> returnDataObjectClass){
		this.redisKey = redisKey;
		this.mongoTableName = mongoTableName;
		this.clazz = returnDataObjectClass;

        if (zsetTopRecordCount > 0){
		    RedisClient.getInstance().scheduleRemoveRecordAfterZSetTop(redisKey, zsetTopRecordCount, zsetCleanCacheInterval);
        }
	}

    public void incTopScore(final String id, final double score, final Callable updateMongoCallable, boolean background){
        if (background){
            DBService.getInstance().executeDBRequest(0, new Runnable() {

                @Override
                public void run() {
                    boolean needUpdate = incTopScoreInRedis(id, score);
                    if (needUpdate){
                        if (updateMongoCallable != null){
                            try {
                                updateMongoCallable.call();
                            } catch (Exception e) {
                                log.error("<incTopScore> catch exception while invoke updateMongoCallable", e);
                            }
                        }
                    }
                }
            });
        }
        else{
            boolean needUpdate = incTopScoreInRedis(id, score);
            if (needUpdate){
                if (updateMongoCallable != null){
                    try {
                        updateMongoCallable.call();
                    } catch (Exception e) {
                        log.error("<incTopScore> catch exception while invoke updateMongoCallable", e);
                    }
                }
            }
        }
    }

	public void updateTopScore(final String id, final double score, final Callable updateMongoCallable, final boolean replaceOnlyHigher, boolean background){
		if (background){
			DBService.getInstance().executeDBRequest(0, new Runnable() {
				
				@Override
				public void run() {
					boolean needUpdate = updateTopScoreInRedis(id, score, replaceOnlyHigher);
					if (needUpdate){
						if (updateMongoCallable != null){
							try {
								updateMongoCallable.call();
							} catch (Exception e) {
								log.error("<updateTopScore> catch exception while invoke updateMongoCallable", e);
							}
						}
					}
				}
			});
		}
		else{
			boolean needUpdate = updateTopScoreInRedis(id, score, replaceOnlyHigher);
			if (needUpdate){
				if (updateMongoCallable != null){
					try {
						updateMongoCallable.call();
					} catch (Exception e) {
						log.error("<updateTopScore> catch exception while invoke updateMongoCallable", e);
					}
				}
			}
		}
	}
		
	private List<T> getListFromMongo(MongoDBClient mongoClient, List<ObjectId> idList, BasicDBObject returnFields, int offset, int limit) {
		/*offset = 0;
		limit = idList.size();*/
		BasicDBObject query = new BasicDBObject(this.mongoIdFieldName, new BasicDBObject("$in", idList));
		DBCursor cursor = mongoClient.find(this.mongoTableName, query, returnFields, null, offset, limit);
		log.info("<getListFromMongo> query = " + query);

		Map<ObjectId, T> map = new HashMap<ObjectId, T>();
		if (cursor != null) {
			while (cursor.hasNext()) {
				DBObject dbObject = (DBObject) cursor.next();
				T t = newClassInstance();
				t.setDbObject(dbObject);
				map.put(t.getObjectId(), t);
			}
			cursor.close();
		}
		
		// sort data by using id sequence
		List<T> retList = new ArrayList<T>();
		for (ObjectId uid : idList){
			if (map.containsKey(uid)){
				retList.add(map.get(uid));
			}
		}

		return retList;
	}

    public List<ObjectId> getTopIdList(final int offset, final int limit){
        log.info("<getTopIdList> redis key = "+redisKey);
        Set<String> set = RedisClient.getInstance().ztop(redisKey, offset, limit);
        if (set == null || set.size() == 0){
            log.info("<getTopIdList> no record fond");
            return Collections.emptyList();
        }

        List<ObjectId> idList = new ArrayList<ObjectId>();
        for (String id : set){
            idList.add(new ObjectId(id));
        }
        log.info("<getTopIdList> total "+idList.size()+" found");

        return idList;
    }
	
	public List<T> getTopList(final int offset, final int limit, String deleteStatusFieldName,  int deleteStatusValue, BasicDBObject returnMongoFields){
		log.info("<getTopList> redis key = "+redisKey);
		Set<String> set = RedisClient.getInstance().ztop(redisKey, offset, limit);
		if (set == null || set.size() == 0){
			log.info("<getTopList> no record found");
			return Collections.emptyList();
		}
		
		List<ObjectId> idList = new ArrayList<ObjectId>();
		for (String id : set){
			idList.add(new ObjectId(id));
		}
		log.info("<getTopList> total "+idList.size()+" found");
	
		return getIdListUtils.getList(DBService.getInstance().getMongoDBClient(), mongoTableName, mongoIdFieldName, 
				deleteStatusFieldName, deleteStatusValue,
				idList, returnMongoFields, clazz);
	}

    public T getSingle(final String member, BasicDBObject returnMongoFields){

        if (StringUtil.isEmpty(member)){
            log.warn("<getSingle> but member is null");
            return null;
        }

        log.info("<getSingle> redis key = "+redisKey);
        int rank = RedisClient.getInstance().zrevrank(redisKey, member);
        if (rank == RedisClient.RANK_NOT_FOUND){
            log.warn("<getSingle> but member not in rank list");
        }

        T t = getIdListUtils.getSingle(DBService.getInstance().getMongoDBClient(),
                mongoTableName,
                mongoIdFieldName,
                new ObjectId(member),
                returnMongoFields,
                clazz);

        if (t == null){
            return null;
        }

        t.getDbObject().put(DBConstants.F_RANK, rank);  // 追加redis的用户排名，合并到mongodb数据一起返回
        return t;
    }
	
	public void deleteIndex(final String id, final boolean background){
		if (background){
			DBService.getInstance().executeDBRequest(0, new Runnable() {
				@Override
				public void run() {
					RedisClient.getInstance().zrem(redisKey, id);				
				}
			});
		}
		else{
			RedisClient.getInstance().zrem(redisKey, id);			
		}
	}

    private boolean incTopScoreInRedis(final String id, final double incScore) {

        Object result = (Object)RedisClient.getInstance().execute(new RedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                if (redisKey == null || id == null){
                    log.error("<incTopScoreInRedis> ADD but key or member is null");
                    return Boolean.FALSE;
                }

                jedis.zincrby(redisKey, incScore, id);
                log.info("<incTopScoreInRedis> "+id+","+incScore+" ADDED @"+redisKey);
                return Boolean.TRUE;
            }
        });

        if (result == null)
            return false;

        return ((Boolean)result).booleanValue();
    }

	private boolean updateTopScoreInRedis(final String id, final double score, final boolean replaceOnlyHigher) {
		
		Object result = (Object)RedisClient.getInstance().execute(new RedisCallable<Boolean>() {
			@Override
			public Boolean call(Jedis jedis) {				
				if (redisKey == null || id == null){
					log.error("<updateTopScoreInRedis> ADD but key or member is null");
					return Boolean.FALSE;
				}
				
				if (replaceOnlyHigher){
					Double currentScore = jedis.zscore(redisKey, id);
					if (currentScore == null || score - currentScore.doubleValue() > 0.0001){
						jedis.zadd(redisKey, score, id);
						log.info("<updateTopScoreInRedis> "+id+","+score+" ADDED @"+redisKey);
						return Boolean.TRUE;
					}
					else{
						log.info("<updateTopScoreInRedis> "+id+","+score+" score lower than current");
						return Boolean.FALSE;
						
					}
				}
				else{
					jedis.zadd(redisKey, score, id);
					log.info("<updateTopScoreInRedis> "+id+","+score+" ADDED @"+redisKey);
					return Boolean.TRUE;					
				}
				
			}			
		});		
		
		if (result == null)
			return false;
		
		return ((Boolean)result).booleanValue();		
	}

	public int getCurrentTopCount() {
		int count =  RedisClient.getInstance().ztopcount(this.redisKey);
		log.info("<getCurrentTopCount> count="+count);
		return count;
	}
	
	
	 public int getZsetTopRecordCount() {
			return zsetTopRecordCount;
		}

    public int getZsetMemberRank(String member){

        int rank =  RedisClient.getInstance().zrank(this.redisKey, member);
        log.info("<getZsetMemberRank> rank="+rank);
        return rank;
    }

    public int getZsetMemberRevRank(String member){

        int rank =  RedisClient.getInstance().zrevrank(this.redisKey, member);
        log.info("<getZsetMemberRank> rank="+rank);
        return rank;
    }


    protected void removeMember(String member) {
        redisClient.zrem(redisKey, member);
    }
}
