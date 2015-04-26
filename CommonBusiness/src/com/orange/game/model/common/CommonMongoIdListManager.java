package com.orange.game.model.common;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.service.DBService;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.*;

public abstract class CommonMongoIdListManager<T extends CommonData> {

    public static final int ALLOW_DUPLICATE = 1;
    public static final int NOT_ALLOW_DUPLICATE = 0;
    public static final int INDEX_NOT_DONE = 0;

    /*
     * 表名
        total_count
        new_count字段名
        id列表字段名
        关联的表名（opus或者opus_action）
     *
     */
    public static final int INDEX_ONGOING = 1;
    public static final int INDEX_COMPLETE = 2;
    public static final int INDEX_FAILURE = 3;
    public static final int INDEX_CAN_UPSERT = 4;
    protected final MongoDBClient mongoDBClient = DBService.getInstance().getMongoDBClient();
    final protected Class<T> clazz;
    final MongoGetIdListUtils<T> getIdListUtils = new MongoGetIdListUtils<T>();
    final String idListTableName;
    final String idTableName;
    protected Logger log = Logger.getLogger(CommonMongoIdListManager.class.getName());
    protected boolean isReverseRead = true;
    protected boolean useObjectIdForKey = true;                                            // 当前支持的ID和LIST都是是用ObjectId的
    protected boolean useObjectIdForIdList = true;
    protected int isListIdAllowDuplicate = ALLOW_DUPLICATE;         // 是否允许list里面的ID重复
    protected boolean autoFixTotalSize = false;                     // 是否尝试自动修复totalCount字段
    String keyFieldName = DBConstants.F_OWNER;
    String listFieldName = DBConstants.F_ID_LIST;
    String totalCountField = DBConstants.F_TOTAL_COUNT;                // 一共有多少条记录
    String unreadCountField = DBConstants.F_UNREAD_COUNT;        // 多少条未读记录
    String modifyDateField = DBConstants.F_MODIFY_DATE;            // 最近一次修改日期（增加或者删除）
    String readDateField = DBConstants.F_READ_DATE;                    // 最近一次阅读日期
    String mongoIdFieldName = "_id";


    public CommonMongoIdListManager(String idListTableName, String idTableName, Class<T> returnDataObjectClass) {
        this.idListTableName = idListTableName;
        this.idTableName = idTableName;
        this.clazz = returnDataObjectClass;

        createDBIndexIfNotExist(idListTableName, keyFieldName);
    }

    public String getIdListTableName() {
        return idListTableName;
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    public String getListFiledName(){
        return listFieldName;
    }

    private void createDBIndexIfNotExist(String idListTableName, String keyFieldName) {
        DBService.getInstance().getMongoDBClient().createIndexIfNotExist(idListTableName, keyFieldName, true);
    }

    ;

    public void fixTotalSize(String key) {
        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null) {
            log.warn(this.idListTableName + "<fixTotalSize> but key or id is null");
            return;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        BasicDBObject dbObject = (BasicDBObject) DBService.getInstance().getMongoDBClient().findOne(idListTableName, query);
        if (dbObject == null) {
            log.warn(this.idListTableName + "<fixTotalSize> but key " + key + " not found in DB");
            return;
        }

        BasicDBList list = (BasicDBList) dbObject.get(this.listFieldName);
        int actualTotalSize = 0;
        if (list != null) {
            actualTotalSize = list.size();
        }

        int currentTotalSize = dbObject.getInt(this.totalCountField);
        if (currentTotalSize != actualTotalSize) {
            // need fix
            log.info(this.idListTableName + "<fixTotalSize> try to fix key " + key + ", actual size is " + actualTotalSize + ", current size is " + currentTotalSize);
            BasicDBObject update = new BasicDBObject("$set", new BasicDBObject(this.totalCountField, actualTotalSize));
            DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);
        } else {
            // dont't need fix
            log.info(this.idListTableName + "<fixTotalSize> no need to fix key " + key + ", size is " + actualTotalSize);
        }

    }

    // empty list
    public void clearList(String key) {

//        Object keyObjectId = getId(key, useObjectIdForKey);
//        if (keyObjectId == null){
//            log.warn(this.idListTableName+"<clearList> but key or id is null");
//            return;
//        }
//
//        DBObject query = new BasicDBObject();
//        query.put(keyFieldName, keyObjectId);
//
//        DBObject update = new BasicDBObject();
//        DBObject updateValue = new BasicDBObject();
//        updateValue.put(listFieldName, new BasicDBList());
//        updateValue.put(totalCountField, 0);
//        updateValue.put(unreadCountField, 0);
//        updateValue.put(modifyDateField, new Date());
//        updateValue.put(readDateField, new Date());
//
//        update.put("$set", updateValue);
//        DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);

        clearList(key, null);
    }

    // empty list
    public void clearList(String key, Map<String, Object> clearObjectMap) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null) {
            log.warn(this.idListTableName + "<clearList> but key or id is null");
            return;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        DBObject update = new BasicDBObject();
        DBObject updateValue = new BasicDBObject();
        updateValue.put(listFieldName, new BasicDBList());
        updateValue.put(totalCountField, 0);
        updateValue.put(unreadCountField, 0);
        updateValue.put(modifyDateField, new Date());
        updateValue.put(readDateField, new Date());

        if (clearObjectMap != null) {
            updateValue.putAll(clearObjectMap);
        }

        update.put("$set", updateValue);
        DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);
    }

    public List<ObjectId> getAllIdList(String key) {
        DBObject obj = mongoDBClient.findOne(getIdListTableName(), keyFieldName, new ObjectId(key));
        if (obj != null) {
            return (List<ObjectId>) obj.get(listFieldName);
        }
        return Collections.emptyList();
    }

    public long getTotalCount(String key) {
        return getCountData(key).getTotalCount();
    }

    public long getUnreadCount(String key) {
        return getCountData(key).getNewCount();
    }

    protected Object getId(String stringId, boolean useObjectId) {
        if (stringId == null)
            return null;

        if (useObjectId) {
            if (!ObjectId.isValid(stringId)) {
                log.warn(this.idListTableName + "<getId> but stringId" + stringId + " is not valid ObjectId");
                return null;
            }

            return new ObjectId(stringId);
        } else {
            return stringId;
        }
    }

    public void insertEmptyId(final String key) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null) {
            log.warn(this.idListTableName + "<insertEmptyId> but key is null");
            return;
        }

        final DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        final DBObject update = new BasicDBObject();

        DBObject incUpdate = new BasicDBObject();
        incUpdate.put(totalCountField, 0);
        incUpdate.put(unreadCountField, 0);

        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(modifyDateField, new Date());

        update.put("$inc", incUpdate);
        update.put("$set", updateValue);

        DBService.getInstance().executeDBRequest(1, new Runnable() {
            @Override
            public void run() {
            log.info("<insertEmptyId> query="+query.toString()+",update="+update.toString());
            DBService.getInstance().getMongoDBClient().upsertAll(idListTableName, query, update);
            }
        });
    }

    public void insertId(final String key, String id, int allowDupliate, boolean isUpdateNewCount, boolean background, boolean upsert) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        Object valueObjectId = getId(id, useObjectIdForKey);
        if (keyObjectId == null || valueObjectId == null) {
            log.warn(this.idListTableName + "<insertId> but key or id is null");
            return;
        }

        final DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        final DBObject update = new BasicDBObject();

        DBObject pushUpdate = new BasicDBObject();
        pushUpdate.put(listFieldName, valueObjectId);
        if (allowDupliate == 1) {
            update.put("$push", pushUpdate);
        } else {
            update.put("$addToSet", pushUpdate);
        }

        DBObject incUpdate = new BasicDBObject();
        incUpdate.put(totalCountField, 1);
        if (isUpdateNewCount) {
            incUpdate.put(unreadCountField, 1);
        }
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(modifyDateField, new Date());
        update.put("$inc", incUpdate);
        update.put("$set", updateValue);

        if (upsert) {
            if (background){
                DBService.getInstance().executeDBRequest(1, new Runnable() {
                    @Override
                    public void run() {
                        DBService.getInstance().getMongoDBClient().upsertAll(idListTableName, query, update);
                    }
                });
            }
            else{
                DBService.getInstance().getMongoDBClient().upsertAll(idListTableName, query, update);
            }
        } else {
            if (background){
                DBService.getInstance().executeDBRequest(1, new Runnable() {
                    @Override
                    public void run() {
                        DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);
                    }
                });
            }
            else{
                DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);
            }
        }

        if (autoFixTotalSize) {
            DBService.getInstance().executeDBRequest(1, new Runnable() {
                @Override
                public void run() {
                    fixTotalSize(key);
                }
            });
        }
    }

    public void insertId(String key, String id, boolean isUpdateNewCount, boolean background) {
        insertId(key, id, ALLOW_DUPLICATE, isUpdateNewCount, background, true);
    }

    public void insertId(String key, String id, int allowDuplicate, boolean isUpdateNewCount, boolean background) {
        insertId(key, id, allowDuplicate, isUpdateNewCount, background, true);
    }

    protected boolean insertIdListOnlyNotExist(String key, List<ObjectId> idList, boolean isUpdateNewCount, boolean background) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null || idList == null || idList.size() == 0) {
            log.warn(this.idListTableName + "<insertIdListOnlyNotExist> but key or list is null or empty");
            return true;
        }


        DBObject obj = new BasicDBObject();

        obj.put("_id", keyObjectId);
        obj.put(keyFieldName, keyObjectId);
        obj.put(listFieldName, idList);
        obj.put(totalCountField, idList.size());
        obj.put(unreadCountField, 0);
        obj.put(modifyDateField, new Date());

        try {
            DBService.getInstance().getMongoDBClient().insert(idListTableName, obj);
            return true;
        } catch (Exception e) {
            // index must be uniqued for keyFieldName make insert failure if exists
            log.error(this.idListTableName + "<insertIdListOnlyNotExist> while create index for key=" + key + ", exception=" + e.toString(), e);
            return false;
        }

    }

    private void insertIdNoDuplicate(String key, String id, boolean isUpdateNewCount, boolean background) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        Object valueObjectId = getId(id, useObjectIdForKey);
        if (keyObjectId == null || valueObjectId == null) {
            log.warn(this.idListTableName + "<insertId> but key or id is null");
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

    public void removeId(String key, String id, boolean background) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        Object valueObjectId = getId(id, useObjectIdForKey);
        if (keyObjectId == null || valueObjectId == null) {
            log.warn(this.idListTableName + "<removeId> but key or id is null");
            return;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        DBObject update = new BasicDBObject();
        DBObject pushUpdate = new BasicDBObject();
        pushUpdate.put(listFieldName, valueObjectId);
        update.put("$pull", pushUpdate);

        DBObject incUpdate = new BasicDBObject();
        incUpdate.put(totalCountField, -1);
        update.put("$inc", incUpdate);
        update.put("$set", new BasicDBObject(modifyDateField, new Date()));

        DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);

        if (autoFixTotalSize) {
            fixTotalSize(key);
        }
    }

    protected DBObject findObjectByIdInList(String key, String id, BasicDBObject returnFields) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        Object valueObjectId = getId(id, useObjectIdForKey);
        if (keyObjectId == null || valueObjectId == null) {
            log.warn(this.idListTableName + "<isIdExistInList> but key or id is null");
            return null;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        BasicDBList inList = new BasicDBList();
        inList.add(valueObjectId);

        query.put(listFieldName, new BasicDBObject("$in", inList));

        DBObject obj = DBService.getInstance().getMongoDBClient().findOne(idListTableName, query, returnFields);
        return obj;
    }

    public boolean isIdExistInList(String key, String id) {

        BasicDBObject returnFields = new BasicDBObject(listFieldName, 0);
        DBObject obj = findObjectByIdInList(key, id, returnFields);
        if (obj == null) {
            log.info("<isIdExistInList> key=" + key + ",id=" + id + ", NOT FOUND");
            return false;
        } else {
            log.info("<isIdExistInList> key=" + key + ",id=" + id + ", FOUND");
            return true;
        }

    }

    public void clearNewCount(String key, boolean background) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null) {
            log.warn(this.idListTableName + "<clearNewCount> but key or id is null");
            return;
        }

        BasicDBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        BasicDBObject setValue = new BasicDBObject(unreadCountField, 0);
        setValue.put(readDateField, new Date());

        BasicDBObject update = new BasicDBObject("$set", setValue);
        DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);
    }

    public IdListData getCountData(String key) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null) {
            log.warn(this.idListTableName + "<getCountData> but key is null");
            return IdListData.ZERO_DATA;
        }

        BasicDBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        BasicDBObject returnFields = new BasicDBObject();
        returnFields.put(totalCountField, 1);
        returnFields.put(unreadCountField, 1);

        DBCursor cursor = DBService.getInstance().getMongoDBClient().findAll(idListTableName, query, returnFields);
        if (cursor == null) {
            return IdListData.ZERO_DATA;
        }

        if (cursor.hasNext() == false){
            cursor.close();
            return IdListData.ZERO_DATA;
        }

        BasicDBObject obj = (BasicDBObject) cursor.next();
        //Object totalObject = obj.get(totalCountField);
        int totalCount = getInt(totalCountField, obj);
        /*if (totalObject!=null) {
			totalCount = (Integer) totalObject;
		}*/
        //Object unreadCountObject = obj.get(unreadCountField);
        int unreadCount = getInt(unreadCountField, obj);
		/*if (unreadCountObject != null) {
			unreadCount = (Integer) unreadCountObject;
		}*/
        cursor.close();

        return new IdListData(totalCount, unreadCount);
    }

    public BasicDBObject getObject(String key, BasicDBObject returnFields) {

        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null) {
            log.warn(this.idListTableName + "<getCountData> but key is null");
            return null;
        }

        BasicDBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        DBCursor cursor = DBService.getInstance().getMongoDBClient().findAll(idListTableName, query, returnFields);
        if (cursor == null) {
            return null;
        }

        if (cursor.hasNext() == false){
            cursor.close();
            return null;
        }

        BasicDBObject obj = new BasicDBObject((BasicDBObject) cursor.next());
        cursor.close();
        return obj;
    }

    abstract protected String indexBeforeDate();

    private int isIndexDone(final String key) {

        if (!StringUtil.isEmpty(indexBeforeDate())) {
            if (DateUtil.idAfterDate(key, indexBeforeDate())) {
                log.warn(this.idListTableName + " skip create index due to the key is after " + indexBeforeDate());
                return INDEX_COMPLETE;
            }
        }

        // query list from idListTable
        Object keyObjectId = getId(key, useObjectIdForKey);

        if (keyObjectId == null) {
            log.warn(this.idListTableName + "<isIndexDone> but key is null");
            return INDEX_NOT_DONE;

        }

        BasicDBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);
        DBObject object = DBService.getInstance().getMongoDBClient().findOne(idListTableName, query);
        if (object == null) {
            log.info(this.idListTableName + "<isIndexDone> index NOT DONE, objectId = " + keyObjectId.toString());
            return INDEX_NOT_DONE;
        } else {
            log.info(this.idListTableName + "<isIndexDone> index ALREADY DONE, objectId = " + keyObjectId.toString());
            return INDEX_COMPLETE;
        }

    }

    public void removeIndex(String key, boolean background) {
        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null) {
            log.warn(this.idListTableName + "<removeIndex> but key or id is null");
            return;
        }

        DBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        DBService.getInstance().getMongoDBClient().removeOne(idListTableName, query);
    }

    public List<T> getList(final String key, final int offset, final int limit, BasicDBObject returnMongoFields, String deleteStatusFieldName, int deleteStatusValue) {

        // query list from idListTable
        Object keyObjectId = getId(key, useObjectIdForKey);
        if (keyObjectId == null) {
            log.warn(this.idListTableName + "<getList> but key is null");
            return Collections.emptyList();
        }
        log.info(this.idListTableName + "<getList> objectId = " + keyObjectId.toString());

        BasicDBObject query = new BasicDBObject();
        query.put(keyFieldName, keyObjectId);

        int nOffset = 0;
        if (isReverseRead) {
            nOffset = -(offset + limit);
        } else {
            nOffset = offset;
        }

        DBObject object = DBService.getInstance().getMongoDBClient().findOneWithArrayLimit(idListTableName, query, listFieldName, nOffset, limit, null);

        List<ObjectId> objectIds = Collections.emptyList();
        List<String> stringIds = Collections.emptyList();
        if (object != null) {
            //int timeLineCount = (Integer) object.get(totalCountField);
            int timeLineCount = getInt(totalCountField, object);
            if (offset == 0) {
                // TODO should this be moved outside???
                // updateTimeLineReadCount(key, timeLineCount);
                clearNewCount(key, true);
            }

            if (object.get(listFieldName) != null){
                objectIds = (List<ObjectId>) object.get(listFieldName);
                stringIds = (List<String>) object.get(listFieldName);
            }

            if (isReverseRead) {
                if (timeLineCount < -nOffset) {
                    int size = limit + nOffset + timeLineCount;
                    if (size > objectIds.size()) {
                        size = objectIds.size();
                    }

                    if (size > 0 && size <= objectIds.size()) {
                        objectIds = objectIds.subList(0, size);
                        stringIds = stringIds.subList(0, size);
                    } else {
                        objectIds = Collections.emptyList();
                        stringIds = Collections.emptyList();
                    }
                }
            } else {
                // do nothing for non-reverse case
            }
        }
        // query data from id list
        int size = objectIds.size();
        if (useObjectIdForIdList) {
            if (isReverseRead) {
                Collections.reverse(objectIds);
            }
            return getIdListUtils.getList(DBService.getInstance().getMongoDBClient(), idTableName,
                    mongoIdFieldName, deleteStatusFieldName, deleteStatusValue, objectIds, returnMongoFields, clazz);
        } else {
            if (isReverseRead) {
                Collections.reverse(stringIds);
            }
            return getIdListUtils.getListByStringIdList(DBService.getInstance().getMongoDBClient(), idTableName,
                    mongoIdFieldName, deleteStatusFieldName, deleteStatusValue, stringIds, returnMongoFields, 0, size, clazz);
        }
    }

    private boolean constructIndex(String userId, List<T> list) {

        if (list == null){
            list = Collections.emptyList();
        }

        int size = (list == null) ? 0 : list.size();
        log.info(this.idListTableName + "<constructIndex> start to construct " + size + " list for user " + userId);
        List<ObjectId> objectIdList = new ArrayList<ObjectId>();
        for (T obj : list) {
            objectIdList.add(obj.getObjectId());
        }
        boolean result = insertIdListOnlyNotExist(userId, objectIdList, false, false);
        log.info(this.idListTableName + "<constructIndex> complete construct " + list.size() + " list for user " + userId);

        return result;
    }

    protected abstract List<T> invokeOldGetList(String userId, int offset, int limit);
	
	/*
	public void constructIndex(String userId, String opusId) {
		try{
			if (StringUtil.isEmpty(opusId)) {
				log.info("<constructIndex> but opusId is null, just construct a empty record");
				insertEmptyIndex(userId, INDEX_COMPLETE, true);
				return ;
			}
			
			log.info("<constructIndex> start to construct index  user  = "+userId);
			//String uid = userAction.getCreateUserId();
			insertId(userId, opusId, false, false);
			log.info("<constructIndex> complete construct index  for user  = "+userId);
			
			updateIndex(userId, INDEX_COMPLETE, true);
		}catch (Exception e) {
			log.warn("<constructIndex> but fail e = "+e.toString());
			updateIndex(userId, INDEX_FAILURE, true);
		}
	}
	*/

    protected abstract List<T> invokeOldGetListForConstruct(String key);

    protected BasicDBObject returnMongoDBFields() {
        return null;
    }

    protected String deleteStatusFieldName() {
        return null;
    }

    protected int deleteStatusValue() {
        return 0;
    }

    public List<T> getListAndConstructIndex(final String key, final int offset, final int limit) {
        List<T> retList = Collections.emptyList();

        final int indexDone = isIndexDone(key);
        if (indexDone == INDEX_COMPLETE || indexDone == INDEX_CAN_UPSERT) {
            log.info(this.idListTableName + "<getListAndConstructIndex> index done, return list directly " + key);
            retList = getList(key, offset, limit, returnMongoDBFields(), deleteStatusFieldName(), deleteStatusValue());
        } else {
            log.info(this.idListTableName + "<getListAndConstructIndex> index not ready, try to construct one for key " + key);
            retList = invokeOldGetList(key, offset, limit);

            this.constructIndexFromOldData(key, null, false);
        }
        return retList;
    }

    public void constructIndexFromOldData(final String key, final String id, final boolean updateNewCount){
        constructIndexFromOldData(key, id, updateNewCount, false);
    }

    public void constructIndexFromOldData(final String key, final String id, final boolean updateNewCount, final boolean forceCreateIndex) {
        DBService.getInstance().executeDBRequest(0, new Runnable() {
            @Override
            public void run() {
                try {
                    int indexDone = INDEX_NOT_DONE;

                    if (!forceCreateIndex){
                        indexDone = isIndexDone(key);
                    }

//                    if (indexDone == INDEX_CAN_UPSERT){
//                        // try to append a new record
//                        log.info(CommonMongoIdListManager.this.idListTableName+"<constructIndexFromOldData> index empty and can insert, key="+key+", id="+id);
//                        insertId(key, id, isListIdAllowDuplicate, updateNewCount, false, true);    // upsert record
//                        return;
//                    }
//                	else

                    if (indexDone == INDEX_COMPLETE) {
                        // try to append a new record
                        log.info(CommonMongoIdListManager.this.idListTableName + "<constructIndexFromOldData> index exists, append index, key=" + key + ", id=" + id);
                        insertId(key, id, isListIdAllowDuplicate, updateNewCount, false, true);
                        return;
                    }

                    List<T> insertList = Collections.emptyList();
                    insertList = invokeOldGetListForConstruct(key);
                    if (insertList == null || insertList.size() == 0) {
                        insertList = new ArrayList<T>();
                    }

                    log.info("after<invokeOldGetListForConstruct> key = " + key + ", old list size = " + insertList.size());
                    if (id != null && id.length() > 0 && ObjectId.isValid(id)) {
                        boolean found = false;
                        for (T t : insertList) {
                            if (t.getObjectId().toString().equals(id)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            log.info(CommonMongoIdListManager.this.idListTableName + "<constructIndexFromOldData> add one more index, key=" + key + ", id=" + id);
                            T obj = newClassInstance(clazz);
                            obj.setObjectId(new ObjectId(id));
                            insertList.add(obj);
                        }
                    }

                    boolean result = constructIndex(key, insertList);
                    if (result == false && !StringUtil.isEmpty(id)) {
                        // try to append a new record
                        log.info(CommonMongoIdListManager.this.idListTableName + "<constructIndexFromOldData> append index, key=" + key + ", id=" + id);
                        insertId(key, id, isListIdAllowDuplicate, updateNewCount, false, false);
                    }

                } catch (Exception e) {
                    log.error(CommonMongoIdListManager.this.idListTableName + "<constructIndexFromOldData> create index failure, exception = " + e.toString(), e);
                }
            }

        });
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

    /*
	public void constructIndexFromOldData(final String key,final String id,final int indexDone) {
		final int indexDone = isIndexDone(key);
		log.info("<constructIndexFromOldData> index not ready, try to construct one for key "+key);
		// execute index creation at backgrounds
		DBService.getInstance().executeDBRequest(0, new Runnable() {
			
			@Override
			public void run() {
				try {

					List<T> insertList = Collections.emptyList();
                    if (indexDone == INDEX_ONGOING){
                        // index is constructing, don't need to invoke index construction again
                        // this will be executed after index construction is completed
                        log.info("<constructIndexFromOldData> index onogings for key "+key);
                        if (id != null && id.length() > 0 && ObjectId.isValid(id)){
                            insertId(key, id, false, false);
                            log.info("<constructIndexFromOldData> add one more index, key="+key+", id="+id);
                        }
                    }
                    else {

                        isIndexDone(key);

                        if(indexDone == INDEX_FAILURE){
                            log.info("<constructIndexFromOldData> index failure previously, remove old one "+key);
                            removeIndex(key, true);
                        }

                        log.info("<constructIndexFromOldData> start construct index for key "+key);
                        updateIndex(key, INDEX_ONGOING, false);

                        // insert record for monitoring
                        IndexMonitorManager.getInstance().insert(idListTableName, key, id, indexDone);

                        insertList = invokeOldGetListForConstruct(key);

                        constructIndex(key,insertList);

                        if (id != null && id.length() > 0 && ObjectId.isValid(id)){
                            boolean found = false;
                            for (T t : insertList){
                                if (t.getObjectId().toString().equals(id)){
                                    found = true;
                                    break;
                                }
                            }
                            if (!found){
                                insertId(key, id, false, false);
                                log.info("<constructIndexFromOldData> add one more index, key="+key+", id="+id);
                            }
                        }

                        // delete the record for monitoring
                        IndexMonitorManager.getInstance().remove(idListTableName, key, id, indexDone);
                    }

				} catch (Exception e) {
					log.warn("<constructIndexFromOldData> create index fail e = "+e.toString());
					updateIndex(key, INDEX_FAILURE, true);

                    // delete the record for monitoring
                    IndexMonitorManager.getInstance().remove(idListTableName, key, id, indexDone);
				}
			}
		});
	}
	*/

    public void insertAndConstructIndex(final String key, String opusId, boolean updateNewCount) {
        constructIndexFromOldData(key, opusId, updateNewCount);
    }

    /*
	public void insertOnlyIndexExists(String key, String opusId) {
        insertOnlyIndexExists(key, opusId, false);
	}
	*/

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

    public void insertIdIntoOwnerIdList(List<ObjectId> ownerIdList, String id, boolean isUpdateNewCount, int allowDupliate, boolean upsert, boolean background) {
//        Object keyObjectId = getId(key, useObjectIdForKey);
        Object valueObjectId = getId(id, useObjectIdForKey);
        if (ownerIdList == null || ownerIdList.size() == 0 || valueObjectId == null) {
            log.warn(this.idListTableName + "<insertIdIntoOwnerIdList> but key or id is null");
            return;
        }

        BasicDBList inList = new BasicDBList();
        inList.addAll(ownerIdList);

        final DBObject query = new BasicDBObject();
        query.put(keyFieldName, new BasicDBObject("$in", inList));

        final DBObject update = new BasicDBObject();

        DBObject pushUpdate = new BasicDBObject();
        pushUpdate.put(listFieldName, valueObjectId);
        if (allowDupliate == 1) {
            update.put("$push", pushUpdate);
        } else {
            update.put("$addToSet", pushUpdate);
        }

        DBObject incUpdate = new BasicDBObject();
        incUpdate.put(totalCountField, 1);
        if (isUpdateNewCount) {
            incUpdate.put(unreadCountField, 1);
        }
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(modifyDateField, new Date());
        update.put("$inc", incUpdate);
        update.put("$set", updateValue);


        if (upsert) {
            if (background){
                DBService.getInstance().executeDBRequest(1, new Runnable() {
                    @Override
                    public void run() {
                    log.info("<insertIdIntoOwnerIdList> begin");
                    DBService.getInstance().getMongoDBClient().upsertAll(idListTableName, query, update);
                    log.info("<insertIdIntoOwnerIdList> end");
                    }
                });
            }
            else{
                log.info("<insertIdIntoOwnerIdList> begin");
                DBService.getInstance().getMongoDBClient().upsertAll(idListTableName, query, update);
                log.info("<insertIdIntoOwnerIdList> end");
            }
        } else {
            if (background){
                DBService.getInstance().executeDBRequest(1, new Runnable() {
                    @Override
                    public void run() {
                    log.info("<insertIdIntoOwnerIdList> begin");
                    DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);
                    log.info("<insertIdIntoOwnerIdList> end");
                    }
                });
            }
            else{
                log.info("<insertIdIntoOwnerIdList> begin");
                DBService.getInstance().getMongoDBClient().updateAll(idListTableName, query, update);
                log.info("<insertIdIntoOwnerIdList> end");
            }
        }


    }


    public static class IdListData {
        public static IdListData ZERO_DATA = new IdListData(0, 0);
        public int totalCount;
        public int newCount;

        public IdListData(int totalCount, int newCount) {
            this.totalCount = totalCount;
            this.newCount = newCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getNewCount() {
            return newCount;
        }
    }

	
	/*
	private void pushAction(String actionId, String creator, List<ObjectId> uidList) {
		if (StringUtil.isEmpty(actionId) || uidList == null || StringUtil.isEmpty(creator)) {
			return;
		}

		// TODO Due to we can not upsert the ower filed into the table. So, use
		// the stupid method. improve it later. By Gamy

		for (ObjectId oid : uidList) {
			DBObject query = new BasicDBObject();
			query.put(DBConstants.F_OWNER, oid);
			DBObject update = new BasicDBObject();

			DBObject pushUpdate = new BasicDBObject();
			pushUpdate.put(DBConstants.F_ACTION_IDS, new ObjectId(actionId));
			update.put("$push", pushUpdate);

			DBObject incUpdate = new BasicDBObject();
			incUpdate.put(DBConstants.F_TIMELINE_COUNT, 1);
			if (oid.toString().equalsIgnoreCase(creator)) {
				incUpdate.put(DBConstants.F_TIMELINE_READ_COUNT, 1);
			}
			update.put("$inc", incUpdate);
			mongoClient.upsertAll(DBConstants.T_TIMELINE, query, update);
		}
	}

	private void pullAction(String action, List<ObjectId> uidList) {
		DBObject query = new BasicDBObject();
		DBObject inQuery = new BasicDBObject();
		inQuery.put("$in", uidList);
		query.put(DBConstants.F_OWNER, inQuery);

		DBObject update = new BasicDBObject();
		DBObject pushUpdate = new BasicDBObject();
		pushUpdate.put(DBConstants.F_ACTION_IDS, new ObjectId(action));
		update.put("$pull", pushUpdate);
		
		DBObject incUpdate = new BasicDBObject();
		incUpdate.put(DBConstants.F_TIMELINE_COUNT, -1);
		update.put("$inc", incUpdate);
		
		mongoClient.updateAll(DBConstants.T_TIMELINE, query, update);
	}
	*/
}
