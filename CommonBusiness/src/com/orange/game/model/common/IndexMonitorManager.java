package com.orange.game.model.common;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.manager.config.ConfigManager;
import com.orange.game.model.service.DBService;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-23
 * Time: 下午2:04
 * To change this template use File | Settings | File Templates.
 *
 *
 * server_id
 * table_name
 * key
 * index status
 * ensureIndex({owner:1})
 *
 *
 */
public class IndexMonitorManager {

    protected Logger log = Logger.getLogger(IndexMonitorManager.class.getName());

    private static IndexMonitorManager ourInstance = new IndexMonitorManager();

    public static IndexMonitorManager getInstance() {
        return ourInstance;
    }

    private IndexMonitorManager() {
        DBService.getInstance().getMongoDBClient().createIndexIfNotExist(DBConstants.T_INDEX_MONITOR, DBConstants.F_SERVER_ID, false);
        DBService.getInstance().getMongoDBClient().createIndexIfNotExist(DBConstants.T_INDEX_MONITOR, DBConstants.F_OWNER, false);
    }

    public void insert(String idListTableName, String key, String id, int indexDone) {
        if (idListTableName == null || key == null || !ObjectId.isValid(key))
            return;

        ObjectId keyObjectId = new ObjectId(key);
        BasicDBObject query = new BasicDBObject(DBConstants.F_OWNER, keyObjectId);

        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_OWNER, keyObjectId);
        obj.put(DBConstants.F_SERVER_ID, ConfigManager.getInstance().getServerId());
        obj.put(DBConstants.F_TABLE_NAME, idListTableName);
        obj.put(DBConstants.F_INDEX_DONE, indexDone);
        obj.put(DBConstants.F_MODIFY_DATE, new Date());

        BasicDBObject update = new BasicDBObject("$set", obj);

        DBService.getInstance().getMongoDBClient().upsertAll(DBConstants.T_INDEX_MONITOR, query, update);
        log.info("insert monitor index record=" + obj.toString());

    }

    public void remove(String idListTableName, String key, String id, int indexDone) {

        if (idListTableName == null || key == null || !ObjectId.isValid(key))
            return;

        ObjectId keyObjectId = new ObjectId(key);
        BasicDBObject query = new BasicDBObject(DBConstants.F_OWNER, keyObjectId);
        DBService.getInstance().getMongoDBClient().remove(DBConstants.T_INDEX_MONITOR, query);
        log.info("remove monitor index record, key=" + key);
    }

    public List<ObjectId> getIndexList() {
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_SERVER_ID, ConfigManager.getInstance().getServerId());

        DBCursor cursor = DBService.getInstance().getMongoDBClient().find(DBConstants.T_INDEX_MONITOR, query, null, null, 0, 0);
        if (cursor == null)
            return Collections.emptyList();

        List<ObjectId> retList = new ArrayList<ObjectId>();
        while (cursor.hasNext()){
            BasicDBObject obj = (BasicDBObject)cursor.next();
            ObjectId key = (ObjectId)obj.get(DBConstants.F_OWNER);
            if (key != null){
                retList.add(key);
            }
            else{
                log.info("index monitor, getIndexList but owner field is null, obj="+obj.toString());
            }
        }
        cursor.close();;
        return retList;

    }

    public List<String> getTableList() {
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_SERVER_ID, ConfigManager.getInstance().getServerId());

        DBCursor cursor = DBService.getInstance().getMongoDBClient().find(DBConstants.T_INDEX_MONITOR, query, null, null, 0, 0);
        if (cursor == null)
            return Collections.emptyList();

        List<String> retList = new ArrayList<String>();
        while (cursor.hasNext()){
            BasicDBObject obj = (BasicDBObject)cursor.next();
            String tableName = (String)obj.get(DBConstants.F_TABLE_NAME);
            if (tableName != null && tableName.length() > 0){
                retList.add(tableName);
            }
            else{
                log.info("index monitor, getTableList but table name is null, obj="+obj.toString());
            }
        }
        cursor.close();
        return retList;

    }

    public void resetOngoingIndex(){


        List<String> tables = IndexMonitorManager.getInstance().getTableList();
        List<ObjectId> objectIds = IndexMonitorManager.getInstance().getIndexList();
        if (objectIds.size() == 0 || tables.size() == 0){
            log.info("<resetOngoingIndex> but no ongoing index executing");
            return;
        }

        BasicDBObject inQuery = new BasicDBObject();
        inQuery.put("$in", objectIds);

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_OWNER, inQuery);
        query.put(DBConstants.F_INDEX_DONE, CommonMongoIdListManager.INDEX_ONGOING);

        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_MODIFY_DATE, new Date());
        updateValue.put(DBConstants.F_INDEX_DONE, CommonMongoIdListManager.INDEX_FAILURE);

        DBObject update = new BasicDBObject();
        update.put("$set", updateValue);

        for (String table : tables){
            log.info("<resetOngoingIndex> query="+query.toString()+", update="+update.toString());
            DBService.getInstance().getMongoDBClient().updateAll(table, query, update);
        }


    }
}
