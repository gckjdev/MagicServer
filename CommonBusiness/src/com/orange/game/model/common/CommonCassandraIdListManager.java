package com.orange.game.model.common;

import com.orange.common.utils.StringUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.cassandra.CassandraClient;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.service.DBService;
import me.prettyprint.hector.api.beans.HColumn;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-26
 * Time: 下午4:12
 * To change this template use File | Settings | File Templates.
 */
public abstract class CommonCassandraIdListManager<T extends CommonData> {

    protected static Logger log = Logger.getLogger(CommonCassandraIdListManager.class.getName());

    final MongoGetIdListUtils<T> getIdListUtils = new MongoGetIdListUtils<T>();

    final String columnFamilyName;

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

    public CommonCassandraIdListManager(String columnFamilyName, String mongoTableName, Class<T> returnDataObjectClass){
        this.columnFamilyName = columnFamilyName;
        this.mongoTableName = mongoTableName;
        this.clazz = returnDataObjectClass;
    }

    public void insertIndex(String key, String name, String value){
        CassandraClient.getInstance().insert(columnFamilyName, key, name, value);
    }

    public void deleteIndex(String key, String name){
        CassandraClient.getInstance().deleteStringColumn(columnFamilyName, key, name);
    }

    public List<T> getList(final String key, final String startOffsetId, final String endOffsetId, final int limit, String deleteStatusFieldName,  int deleteStatusValue, BasicDBObject returnMongoFields){

        if (StringUtil.isEmpty(key)){
            return Collections.emptyList();
        }

        log.info("cassandra <getList> column family name = "+columnFamilyName);
        List<HColumn<String, String>> result = null;

        if (StringUtil.isEmpty(startOffsetId) && StringUtil.isEmpty(endOffsetId)){
            result = CassandraClient.getInstance().getColumnKey(columnFamilyName, key, limit);
        }
        else{
            result = CassandraClient.getInstance().getColumnKeyByStringRange(columnFamilyName, key, startOffsetId, endOffsetId, limit);
        }

        if (result == null || result.size() == 0){
            return Collections.emptyList();
        }

        List<ObjectId> idList = new ArrayList<ObjectId>();
        for (HColumn<String, String> column : result){
            String name = column.getName();
            if (name != null && ObjectId.isValid(name)){
                idList.add(new ObjectId(name));
            }
        }
        log.info("cassandra <getList> total "+idList.size()+" found");

        return getIdListUtils.getList(DBService.getInstance().getMongoDBClient(), mongoTableName, mongoIdFieldName,
                deleteStatusFieldName, deleteStatusValue,
                idList, returnMongoFields, clazz);
    }

}
