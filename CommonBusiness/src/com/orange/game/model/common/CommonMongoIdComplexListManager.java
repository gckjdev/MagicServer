package com.orange.game.model.common;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.service.DBService;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-25
 * Time: 下午12:33
 * To change this template use File | Settings | File Templates.
 */
public abstract class CommonMongoIdComplexListManager<T> extends CommonMongoIdListManager {

    final String idListKeyFieldName;
    public boolean useObjectIdForListKey = true;

    public CommonMongoIdComplexListManager(String idListTableName, String idTableName, String idListKeyFieldName, Class returnDataObjectClass) {
        super(idListTableName, idTableName, returnDataObjectClass);
        this.idListKeyFieldName = idListKeyFieldName;
    }

    private Object getIdFromObject(BasicDBObject object) {
        if (object == null)
            return null;

        Object objId = object.get(idListKeyFieldName);
        if (!useObjectIdForListKey){
            if (objId instanceof String) {
                return (String) objId;
            }
            else{
                return null;
            }
        }
        else if (objId instanceof ObjectId){
            return (ObjectId)objId;
        }
        else{
            return null;
        }
    }

    public void insertObject(String key, BasicDBObject object, boolean allowDuplicate, boolean isUpdateNewCount, boolean background){

        Object keyObjectId = getId(key, true);
        Object objectObjectId = getIdFromObject(object);
        if (keyObjectId == null || objectObjectId == null){
            log.warn("<insertObject> but key or objectObjectId is null, key="+key+", object="+object);
            return;
        }

        boolean found = false;
        if (!allowDuplicate){
            // find the object firstly
            BasicDBObject query = new BasicDBObject();
            query.put(keyFieldName, keyObjectId);
            query.put(listFieldName+"."+idListKeyFieldName, object.get(idListKeyFieldName));

            BasicDBObject returnFields = new BasicDBObject("_id", 1);
            DBObject obj = DBService.getInstance().getMongoDBClient().findOne(idListTableName, query, returnFields);
            if (obj != null){
                found = true;
            }
            else{
                found = false;
            }
        }

        if (found){
            DBObject query = new BasicDBObject();
            query.put(keyFieldName, keyObjectId);
            query.put(listFieldName+"."+idListKeyFieldName, object.get(idListKeyFieldName));

            DBObject update = new BasicDBObject();

            BasicDBObject updateValue = new BasicDBObject();
            for (String objKey : object.keySet()){
                updateValue.put(listFieldName+".$."+objKey, object.get(objKey));
            }
            updateValue.put(modifyDateField, new Date());

            update.put("$set", updateValue);
            DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);
        }
        else{
            DBObject query = new BasicDBObject();
            query.put(keyFieldName, keyObjectId);
            DBObject update = new BasicDBObject();

            DBObject pushUpdate = new BasicDBObject();
            pushUpdate.put(listFieldName, object);
            update.put("$push", pushUpdate);

            DBObject incUpdate = new BasicDBObject();
            incUpdate.put(totalCountField, 1);
            if (isUpdateNewCount) {
                incUpdate.put(unreadCountField, 1);
            }
            BasicDBObject updateValue = new BasicDBObject();
            updateValue.put(modifyDateField, new Date());
            update.put("$inc", incUpdate);
            update.put("$set", updateValue);
            DBService.getInstance().getMongoDBClient().upsertAll(idListTableName, query, update);
        }
    }

    private void insertObject(String key, BasicDBObject object, boolean isUpdateNewCount, boolean background){

        Object keyObjectId = getId(key, true);
        Object objectObjectId = getIdFromObject(object);
        if (keyObjectId == null || objectObjectId == null){
            log.warn("<insertObject> but key or objectObjectId is null, key="+key+", object="+object);
            return;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);
        DBObject update = new BasicDBObject();

        DBObject pushUpdate = new BasicDBObject();
        pushUpdate.put(listFieldName, object);
        update.put("$push", pushUpdate);

        DBObject incUpdate = new BasicDBObject();
        incUpdate.put(totalCountField, 1);
        if (isUpdateNewCount) {
            incUpdate.put(unreadCountField, 1);
        }
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(modifyDateField, new Date());
        update.put("$inc", incUpdate);
        update.put("$set", updateValue);
        DBService.getInstance().getMongoDBClient().upsertAll(idListTableName, query, update);
    }

    public void insertObject(String key, BasicDBObject object, String sortedField, boolean isUpdateNewCount, boolean background){

        Object keyObjectId = getId(key, useObjectIdForKey);
        Object objectObjectId = getIdFromObject(object);
        if (keyObjectId == null || objectObjectId == null){
            log.warn("<insertObject> but key or objectObjectId is null, key="+key+", object="+object);
            return;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        BasicDBList pushList = new BasicDBList();
        pushList.add(object);

        DBObject pushEachUpdate = new BasicDBObject();
        pushEachUpdate.put("$each", pushList);
        if (sortedField != null && sortedField.length() > 0){
        	pushEachUpdate.put("$slice", -10000000);
            pushEachUpdate.put("$sort", new BasicDBObject(sortedField, 1));
            
        }

        BasicDBObject pushUpdate = new BasicDBObject();
        pushUpdate.put(listFieldName, pushEachUpdate);

        DBObject update = new BasicDBObject();
        update.put("$push", pushUpdate);

        DBObject incUpdate = new BasicDBObject();
        incUpdate.put(totalCountField, 1);
        if (isUpdateNewCount) {
            incUpdate.put(unreadCountField, 1);
        }
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(modifyDateField, new Date());
        update.put("$inc", incUpdate);
        update.put("$set", updateValue);
        DBService.getInstance().getMongoDBClient().upsertAll(idListTableName, query, update);
    }

    @Override
    protected DBObject findObjectByIdInList(String key, String id, BasicDBObject returnFields) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        Object valueObjectId = getId(id, useObjectIdForListKey);
        if (keyObjectId == null || valueObjectId == null){
            log.warn(this.idListTableName+"<isIdExistInList> but key or id is null");
            return null;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);
        query.put(listFieldName+"."+idListKeyFieldName, valueObjectId);

        DBObject obj = DBService.getInstance().getMongoDBClient().findOne(idListTableName, query, returnFields);
        return obj;
    }


    /*
    protected void insertIdList(String key, List<ObjectId> idList, boolean isUpdateNewCount, boolean background){

        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null || idList == null || idList.size() == 0){
            log.warn("<insertId> but key or id is null");
            return;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);
        DBObject update = new BasicDBObject();


        DBObject allObject = new BasicDBObject();
        allObject.put("$each", idList);

        DBObject pushUpdate = new BasicDBObject();
        pushUpdate.put(listFieldName, allObject);
        update.put("$push", pushUpdate);

        DBObject incUpdate = new BasicDBObject();
        incUpdate.put(totalCountField, idList.size());
        if (isUpdateNewCount) {
            incUpdate.put(unreadCountField, 1);
        }
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(modifyDateField, new Date());
        update.put("$inc", incUpdate);
        update.put("$set", updateValue);
        DBService.getInstance().getMongoDBClient().upsertAll(idListTableName, query, update);
    }
    */


   /*
    public void insertIdNoDuplicate(String key, String id, boolean isUpdateNewCount, boolean background){

        Object keyObjectId = getId(key, useObjectIdForKey);
        Object valueObjectId = getId(id, useObjectIdForKey);
        if (keyObjectId == null || valueObjectId == null){
            log.warn("<insertId> but key or id is null");
            return;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        DBObject update = new BasicDBObject();

        DBObject pushUpdate = new BasicDBObject();
        pushUpdate.put(listFieldName, valueObjectId);
        update.put("$push", pushUpdate);

        DBObject incUpdate = new BasicDBObject();
        incUpdate.put(totalCountField, 1);
        if (isUpdateNewCount) {
            incUpdate.put(unreadCountField, 1);
        }
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(modifyDateField, new Date());
        updateValue.put(DBConstants.F_INDEX_DONE, INDEX_ONGOING);
        update.put("$inc", incUpdate);
        update.put("$set", updateValue);
        DBService.getInstance().getMongoDBClient().upsertAll(idListTableName, query, update);
    }
    */

    @Override
    public void removeId(String key, String id, boolean background){

        Object keyObjectId = getId(key, useObjectIdForKey);
        Object valueObjectId = getId(id, useObjectIdForListKey);
        if (keyObjectId == null || valueObjectId == null){
            log.warn("<removeId> but key or id is null");
            return;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);
        query.put(listFieldName+"."+idListKeyFieldName, valueObjectId);

//		BasicDBList list = new BasicDBList();
//		list.add(keyObjectId);
//		DBObject inQuery = new BasicDBObject();
//		inQuery.put("$in", list);
//		query.put(listFieldName, inQuery);

        DBObject update = new BasicDBObject();
        DBObject pushUpdate = new BasicDBObject();
        BasicDBObject pullObject = new BasicDBObject();
        pullObject.put(idListKeyFieldName, valueObjectId);
        pushUpdate.put(listFieldName, pullObject);
        update.put("$pull", pushUpdate);

        DBObject incUpdate = new BasicDBObject();
        incUpdate.put(totalCountField, -1);
        update.put("$inc", incUpdate);
        update.put("$set", new BasicDBObject(modifyDateField, new Date()));

        DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);
    }
    
    public List<ObjectId> getObjectIdList(final String key, final int offset, final int limit){
        // query list from idListTable
        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null){
            log.warn("<getObjectIdList> but key is null");
            return Collections.emptyList();
        }
        

        BasicDBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        int nOffset = -(offset + limit);
        DBObject object = DBService.getInstance().getMongoDBClient().findOneWithArrayLimit(idListTableName, query, listFieldName, nOffset, limit, null);

        List<DBObject> objectList = Collections.emptyList();
        List<ObjectId> objectIdList = Collections.emptyList();

        if (object != null) {
            int timeLineCount = (Integer) object.get(totalCountField);

            objectList = (List<DBObject>) object.get(listFieldName);
            if (timeLineCount < -nOffset) {
                int size = limit + nOffset + timeLineCount;
                if (size > 0 && size <= objectList.size()) {
                    objectList = objectList.subList(0, size);
                } else {
                    objectList = Collections.emptyList();
                }
            }
        }
        // query data from id list
        int size = objectList.size();
        Collections.reverse(objectList);
        
        objectIdList = new ArrayList<ObjectId>();
        for (DBObject obj : objectList){
        	ObjectId objId = getObjectIdFromListField(obj); //(ObjectId)obj.get(idListKeyFieldName);
        	if (objId != null){
        		if (objectIdList.indexOf(objId) == -1){ // avoid duplicate id
        			objectIdList.add(objId);
        		}
        	}
        }
        
        log.info("<getObjectIdList> objectId = "+keyObjectId.toString()+", total "+objectIdList.size()+" returned");
        
        return objectIdList;    	
    }

    private String getStringFromListField(DBObject obj) {
        Object retIdObj = obj.get(idListKeyFieldName);
        if (retIdObj == null){
            return null;
        }

        if (!useObjectIdForListKey){
            if (retIdObj instanceof String){
                return (String)retIdObj;
            }
        }

        return null;
    }

    private ObjectId getObjectIdFromListField(DBObject obj) {
        Object retIdObj = obj.get(idListKeyFieldName);
        if (retIdObj == null){
            return null;
        }

        if (useObjectIdForListKey){
            if (retIdObj instanceof ObjectId){
                return (ObjectId)retIdObj;
            }
        }

        return null;
    }

    public List<String> getIdList(final String key, final int offset, final int limit){

        // query list from idListTable
        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null){
            log.warn("<getList> but key is null");
            return Collections.emptyList();
        }
        log.info("<getList> objectId = "+keyObjectId.toString());

        BasicDBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        int nOffset = 0;
        if (isReverseRead){
            nOffset = -(offset + limit);
        }
        else{
            nOffset = offset;
        }

        DBObject object = DBService.getInstance().getMongoDBClient().findOneWithArrayLimit(idListTableName, query, listFieldName, nOffset, limit, null);

        List<DBObject> objectList = Collections.emptyList();
        if (object != null) {
            // int timeLineCount = (Integer) object.get(totalCountField);
            int timeLineCount = getInt(totalCountField, object);
            if (offset == 0) {
                // TODO should this be moved outside???
                // updateTimeLineReadCount(key, timeLineCount);
                clearNewCount(key, true);
            }

            objectList = (List<DBObject>) object.get(listFieldName);
            if (isReverseRead){
                if (timeLineCount < -nOffset) {
                    int size = limit + nOffset + timeLineCount;
                    if (size > objectList.size()){
                        size = objectList.size();
                    }

                    if (size > 0 && size <= objectList.size()) {
                        objectList = objectList.subList(0, size);
                    } else {
                        objectList = Collections.emptyList();
                    }
                }
            }
        }

        // query data from id list
        if (isReverseRead){
            int size = objectList.size();
            Collections.reverse(objectList);
        }

        List<String> idList = new ArrayList<String>();
        for (DBObject dbObject : objectList){
//            ObjectId listKey = (ObjectId)dbObject.get(idListKeyFieldName);
            String listKey = getStringFromListField(dbObject);
            if (listKey != null){
                idList.add(listKey);
            }
        }

        return idList;
    }

    @Override
    public List<T> getList(final String key, final int offset, final int limit, BasicDBObject returnMongoFields, String deleteStatusFieldName, int deleteStatusValue){

        // query list from idListTable
        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null){
            log.warn("<getList> but key is null");
            return Collections.emptyList();
        }
        log.info("<getList> objectId = "+keyObjectId.toString());

        BasicDBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        int nOffset = 0;
        if (isReverseRead){
            nOffset = -(offset + limit);
        }
        else{
            nOffset = offset;
        }

        DBObject object = DBService.getInstance().getMongoDBClient().findOneWithArrayLimit(idListTableName, query, listFieldName, nOffset, limit, null);

        List<DBObject> objectList = Collections.emptyList();
        if (object != null) {
           // int timeLineCount = (Integer) object.get(totalCountField);
        	 int timeLineCount = getInt(totalCountField, object);
        	if (offset == 0) {
                // TODO should this be moved outside???
                // updateTimeLineReadCount(key, timeLineCount);
                clearNewCount(key, true);
            }

            objectList = (List<DBObject>) object.get(listFieldName);
            if (isReverseRead){
                if (timeLineCount < -nOffset) {
                    int size = limit + nOffset + timeLineCount;
                    if (size > objectList.size()){
                        size = objectList.size();
                    }

                    if (size > 0 && size <= objectList.size()) {
                        objectList = objectList.subList(0, size);
                    } else {
                        objectList = Collections.emptyList();
                    }
                }
            }
        }
        // query data from id list
        if (isReverseRead){
            int size = objectList.size();
            Collections.reverse(objectList);
        }
//        return getIdListUtils.getList(DBService.getInstance().getMongoDBClient(), idTableName,
//                mongoIdFieldName, deleteStatusFieldName, deleteStatusValue, objectList, returnMongoFields,clazz);
        return getIdListUtils.getList(DBService.getInstance().getMongoDBClient(), idTableName, mongoIdFieldName, idListKeyFieldName, objectList, deleteStatusFieldName, deleteStatusValue, returnMongoFields, clazz);
    }

    protected void updateIndexObject(String key, String objectKeyValue, BasicDBObject objectData) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        Object objectKeyObjectId = getId(objectKeyValue, useObjectIdForListKey);
        if (keyObjectId == null){
            log.warn("<updateIndexObject> but key is null");
            return;
        }
        log.info("<updateIndexObject> objectId = "+keyObjectId.toString() +", objectKeyObjectId="+objectKeyObjectId.toString());

        BasicDBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);
        query.put(listFieldName+"."+idListKeyFieldName, objectKeyObjectId);

        BasicDBObject updateValue = new BasicDBObject();
        for (String objectKey : objectData.keySet()){
            updateValue.put(listFieldName+".$."+objectKey, objectData.get(objectKey));
        }

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateValue);

        DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);
    }

    protected void updateIndexObject(String key, String objectKeyValue, BasicDBObject objectData, BasicDBObject incData) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        Object objectKeyObjectId = getId(objectKeyValue, useObjectIdForListKey);
        if (keyObjectId == null){
            log.warn("<updateIndexObject> but key is null");
            return;
        }
        log.info("<updateIndexObject> objectId = "+keyObjectId.toString() +", objectKeyObjectId="+objectKeyObjectId.toString());

        BasicDBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);
        query.put(listFieldName+"."+idListKeyFieldName, objectKeyObjectId);

        BasicDBObject updateValue = new BasicDBObject();
        for (String objectKey : objectData.keySet()){
            updateValue.put(listFieldName+".$."+objectKey, objectData.get(objectKey));
        }

        BasicDBObject incValue = new BasicDBObject();
        for (String incObjKey : incData.keySet()){
            incValue.put(listFieldName+".$."+incObjKey, incData.get(incObjKey));
        }

        BasicDBObject update = new BasicDBObject();
        if (updateValue.size() > 0) {
            update.put("$set", updateValue);
        }
        if (incValue.size() > 0){
            update.put("$inc", incValue);
        }

        log.info("<updateIndexObject> query="+query.toString()+", update="+update.toString());
        DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);
    }

    protected DBObject getObjectInfo(String key, String id) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        Object valueObjectId = getId(id, useObjectIdForListKey);
        if (keyObjectId == null || valueObjectId == null){
            log.warn(this.idListTableName+"<isIdExistInList> but key or id is null");
            return null;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);
        query.put(listFieldName+"."+idListKeyFieldName, valueObjectId);

        DBObject obj = DBService.getInstance().getMongoDBClient().findOne(idListTableName, query);
        if (obj == null){
            return null;
        }

        BasicDBList list = (BasicDBList)obj.get(listFieldName);
        if (list == null){
            return null;
        }

        for (Object subObj : list){
            BasicDBObject retObj = (BasicDBObject)subObj;
            if (retObj.get(idListKeyFieldName).toString().equalsIgnoreCase(id)){
                return retObj;
            }
        }

        return null;
    }

}
