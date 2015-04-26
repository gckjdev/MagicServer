package com.orange.game.model.common;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.model.dao.CommonData;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.*;

public class MongoGetIdListUtils<T extends CommonData> {

    static Logger log = Logger.getLogger(MongoGetIdListUtils.class.getName());

    public List<T> getListByStringIdList(MongoDBClient mongoClient, String tableName, String keyFieldName, String deleteStatusFieldName, int deleteStatusValue, List<String> idList, BasicDBObject returnFields, int offset, int limit, Class<T> clazz) {
        List<ObjectId> objectIdList = new ArrayList<ObjectId>();
        for (String id : idList) {
            if (ObjectId.isValid(id)) {
                objectIdList.add(new ObjectId(id));
            }
        }

        return getList(mongoClient, tableName, keyFieldName, deleteStatusFieldName, deleteStatusValue, objectIdList, returnFields, clazz);
    }

    public static ObjectId safeGetObjectId(Object value){
        if (value instanceof String){
            return new ObjectId((String)value);
        }
        else if (value instanceof ObjectId){
            return ((ObjectId)value);
        }
        else{
            log.error("<safeGetObjectId> but value is NOT String or ObjectId, key = "+value.toString());
            return null;
        }
    }

    public List<T> getList(MongoDBClient mongoClient, String tableName, String keyFieldName, String objectKeyFieldName, List<DBObject> objectList, String deleteStatusFieldName, int deleteStatusValue, BasicDBObject returnFields, Class<T> clazz) {

        if (objectList.size() == 0) {
            log.info("<getListFromMongo> but id list is empty");
            return Collections.emptyList();
        }

        // construct object id list
        List<ObjectId> idList = new ArrayList<ObjectId>();
        for (DBObject obj : objectList) {
            ObjectId key = safeGetObjectId(obj.get(objectKeyFieldName));
            if (key != null){
                idList.add(key);
            }
            else{
                log.error("<getListFromMongo> but key invalid"+obj.get(objectKeyFieldName));
            }
        }

        int offset = 0;
        int limit = objectList.size();
        log.info("<getListFromMongo> offset = " + offset + "   ,limit = " + limit);
        BasicDBObject query = new BasicDBObject(keyFieldName, new BasicDBObject("$in", idList));
        DBCursor cursor = mongoClient.find(tableName, query, returnFields, null, offset, limit);
        log.info("<getListFromMongo> query = " + query);
        log.info("<getListFromMongo> id count = " + idList.size());

        boolean isCheckDeleteField = (deleteStatusFieldName != null && deleteStatusFieldName.length() > 0);
        log.info("<getListFromMongo> isCheckDeleteField = " + isCheckDeleteField);

        Map<ObjectId, T> map = new HashMap<ObjectId, T>();
        if (cursor != null) {
            while (cursor.hasNext()) {
                BasicDBObject dbObject = (BasicDBObject) cursor.next();

                // skip deleted record
                if (isCheckDeleteField && dbObject.getInt(deleteStatusFieldName) == deleteStatusValue) {
                    continue;
                }
               /* if (isCheckDeleteField && getInt(deleteStatusFieldName, dbObject) == deleteStatusValue){					
                    continue;
				}
                */

                T t = newClassInstance(clazz);
                t.setDbObject(dbObject);
                map.put(t.getObjectId(), t);
            }
            cursor.close();
        }

        // sort data by using id sequence
        List<T> retList = new ArrayList<T>();
        for (DBObject object : objectList) {
            ObjectId key = safeGetObjectId(object.get(objectKeyFieldName));
            if (key != null){
                if (map.containsKey(key)) {
                    T t = map.get(key);
                    t.getDbObject().putAll(object);     // append extra data into this object
                    retList.add(t);
                }
            }
            else{
                log.error("<getListFromMongo> but key invalid"+object.get(objectKeyFieldName));
            }
        }
        log.info("<getListFromMongo> return data count = " + retList.size());
        return retList;
    }

    public List<T> getList(MongoDBClient mongoClient, String tableName, String keyFieldName, String deleteStatusFieldName, int deleteStatusValue, List<ObjectId> idList, DBObject returnFields, Class<T> clazz) {

        if (idList == null || idList.size() == 0) {
            log.info("<getListFromMongo> but id list is empty");
            return Collections.emptyList();
        }
        int offset = 0;
        int limit = idList.size();
        log.info("<getListFromMongo> tableName = " + tableName + "  ,offset = " + offset + "   ,limit = " + limit);
        BasicDBObject query = new BasicDBObject(keyFieldName, new BasicDBObject("$in", idList));
        DBCursor cursor = mongoClient.find(tableName, query, returnFields, null, offset, limit);
        log.info("<getListFromMongo> tableName = " + tableName + " complete");

        boolean isCheckDeleteField = (deleteStatusFieldName != null && deleteStatusFieldName.length() > 0);

        Map<ObjectId, T> map = new HashMap<ObjectId, T>();
        if (cursor != null) {
            while (cursor.hasNext()) {
                BasicDBObject dbObject = (BasicDBObject) cursor.next();

                // skip deleted record
                if (isCheckDeleteField && dbObject.getInt(deleteStatusFieldName) == deleteStatusValue) {
                    continue;
                }
                /*if (isCheckDeleteField && getInt(deleteStatusFieldName, dbObject) == deleteStatusValue){
					continue;
				}*/

                T t = newClassInstance(clazz);
                t.setDbObject(dbObject);
                //map.put(t.getObjectId(), t);
                map.put((ObjectId) dbObject.get(keyFieldName), t);
            }
            cursor.close();
        }

        // sort data by using id sequence
        List<T> retList = new ArrayList<T>();
        for (ObjectId uid : idList) {
            if (map.containsKey(uid)) {
                retList.add(map.get(uid));
            }
        }
        log.info("<getListFromMongo> return data count = " + retList.size());
        return retList;
    }

    public T getSingle(MongoDBClient mongoClient, String mongoTableName, String mongoIdFieldName, ObjectId objectId, BasicDBObject returnMongoFields, Class<T> clazz) {

        log.info("<getSingle> mongoTableName = " + mongoTableName + " ,objectId = " + objectId.toString());
        BasicDBObject query = new BasicDBObject(mongoIdFieldName, objectId);
        DBCursor cursor = mongoClient.find(mongoTableName, query, returnMongoFields, null, 0, 1);
        log.info("<getSingle> query = " + query);

        T t = null;
        if (cursor != null) {
            while (cursor.hasNext()) {
                // only fetch one record
                BasicDBObject dbObject = (BasicDBObject) cursor.next();
                t = newClassInstance(clazz);
                t.setDbObject(dbObject);
                break;
            }
            cursor.close();
        }

        if (t != null) {
            log.info("<getSingle> data found, return data = " + t.getDbObject().toString());
        } else {
            log.info("<getSingle> data not found for objectId=" + objectId.toString());
        }

        return t;
    }

    public List<T> getList(MongoDBClient mongoClient, String tableName, DBObject query, DBObject orderBy, DBObject returnFields, int offset, int limit, Class<T> clazz) {
        DBCursor cursor = mongoClient.find(tableName, query, returnFields, orderBy, offset, limit);
        if (cursor != null) {
            List<T> list = new ArrayList<T>();
            while (cursor.hasNext()) {
                DBObject obj = cursor.next();
                T model = newClassInstance(clazz);
                model.setDbObject(obj);
                if (model != null){
                    list.add(model);
                }
            }
            cursor.close();
            return list;
        }
        return Collections.emptyList();
    }

    private T newClassInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            log.error("<newClassInstance> catch exception " + e.toString(), e);
            return null;
        } catch (IllegalAccessException e) {
            log.error("<newClassInstance> catch exception" + e.toString(), e);
            return null;
        }
    }

    protected int getInt(String key, DBObject dbObject) {
        Object obj = dbObject.get(key);
        if (obj == null) {
            return 0;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        } else if (obj instanceof Double) {
            return ((Double) obj).intValue();
        } else {
            return 0;
        }
    }


}
