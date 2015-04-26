package com.orange.game.api.service;

import com.orange.barrage.common.CommonModelService;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.common.utils.StringUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.elasticsearch.ESIndexBuilder;
import com.orange.common.elasticsearch.ESORMable;
import com.orange.common.elasticsearch.ESQueryBuilder;
import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.manager.bbs.BBSManager;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.util.*;


public class ElasticsearchService extends CommonModelService {

    public static boolean addOrUpdateIndex(final ESORMable model, final MongoDBClient mongoClient) {

        if (model == null) {
            return false;
        }
        Map<String, Object> dataBean = model.getESORM();
        if (dataBean == null) {
            return false;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json;
            try {
                json = mapper.writeValueAsString(dataBean);
            } catch (Exception e) {
                log.error("<addOrUpdateIndex> catch exception = " + e, e);
                return false;
            }
            return ESIndexBuilder.createIndex(json, model.getESIndexName(), model.getESIndexType(), model.getID());
        } catch (Exception e) {
            log.error("<addOrUpdateIndex> catch exception = " + e, e);
            return false;
        }
    }

    public static boolean deleteIndex(ESORMable model, MongoDBClient mongoDBClient) {
        if (model != null) {
            return ESIndexBuilder.deleteIndex(model.getESIndexName(), model.getESIndexType(), model.getID());
        }
        return false;
    }

    public static <T extends CommonData & ESORMable> void indexMongodbToES(String dbName, String tableName, DBObject returnFields, Class<T> clazz) throws IllegalAccessException, InstantiationException {

        MongoDBClient dbClient = new MongoDBClient(dbName);
        int successCount = 0;
        int totoal = 0;

        if (dbClient != null) {

            ServerLog.info(0, "<indexMongodbToES> start, db=" + dbName + ", table=" + tableName);

            DBObject query = new BasicDBObject();
            DBCursor cursor = dbClient.findAll(tableName, query, returnFields);
            if (cursor != null) {
                totoal = cursor.count();
                int doneCount = 0;
                while (cursor.hasNext()) {
                    DBObject dbObject = (DBObject) cursor.next();
                    T model = clazz.newInstance();
                    model.setDbObject(dbObject);

                    if (model.canBeIndexed() && addOrUpdateIndex(model, dbClient)) {
                        successCount++;
                    }
                    doneCount++;

                    if (doneCount % 100 == 0) {
                        ServerLog.info(0, "<indexMongodbToES> Total: " + doneCount + " done, success " + successCount);
                    }
                }
                cursor.close();
            }
            ServerLog.info(0, " All done ! Total " + totoal + ", success " + successCount);
        }
        return;
    }

    public static void main(String[] args) {

        // Test index
//		indexMongodbToES(DBConstants.D_GAME, DBConstants.T_USER, UserManager.getUserPublicReturnFields());

        try {
//            indexMongodbToES(DBConstants.D_GAME, DBConstants.T_USER, UserManager.getUserPublicReturnFields(), User.class);
            indexMongodbToES(DBConstants.D_GAME, DBConstants.T_BBS_POST, BBSManager.getPostSearchFields(), BBSPost.class);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static List<ObjectId> search(String keyString,
                                        List<String> candidateFields,
                                        String returnFieldName,
                                        String filterKey,
                                        String filterValue,
                                        int offset,
                                        int limit,
                                        String... indexType) {

        if (StringUtil.isEmpty(keyString) || candidateFields == null || candidateFields.isEmpty()) {
            log.warn("<search> but key or fields is empty or null");
            return Collections.emptyList();
        }

        SearchResponse searchResponse = ESQueryBuilder.searchByQueryString(
                BarrageConstants.ES_INDEX_NAME_BARRAGE, candidateFields, keyString, filterKey, filterValue, offset, limit, indexType);

        if (searchResponse != null && searchResponse.getHits() != null) {
            long totalHits = searchResponse.getHits().totalHits();
            SearchHit[] hits = searchResponse.getHits().hits();
            if (hits == null) {
                log.info("<search> no result found for " + keyString);
                return Collections.emptyList();
            } else {
                log.info("<search> get " + hits.length + " records for " + keyString);
            }

            List<ObjectId> results = new ArrayList<ObjectId>();
            for (int i = 0; i < hits.length; i++) {
                SearchHit searchHits = hits[i];
                String key = (String) searchHits.getSource().get(returnFieldName);
                if (StringUtil.isEmpty(key) || !ObjectId.isValid(key)) {
                    log.warn("<search> but key is empty or not valid objectId, key= " + key + "fieldName = "+ returnFieldName);
                    continue;
                }
                results.add(new ObjectId(key));
            }
            log.info("<search> final return " + results.size() + " records for " + keyString);
            return results;
        } else {
            log.warn("<search> response null or no hits for "+keyString);
        }

        return Collections.emptyList();

    }

    public static List<Map<String, Object>> searchAndReturnMap(String keyString, List<String> candidateFields, List<String> returnFields, String filterKey, String filterValue, int offset, int limit, String... indexType) {
        if (StringUtil.isEmpty(keyString) || candidateFields == null || candidateFields.isEmpty()) {
            return Collections.emptyList();
        }
        SearchResponse searchResponse = ESQueryBuilder.searchByQueryString(
                DBConstants.ES_INDEX_NAME, candidateFields, keyString, filterKey, filterValue, offset, limit, indexType);

        if (searchResponse != null && searchResponse.getHits() != null) {
            long totalHits = searchResponse.getHits().totalHits();
            SearchHit[] hits = searchResponse.getHits().hits();
            if (hits == null) {
                ServerLog.info(0, "<search> No result found for " + keyString);
                return Collections.emptyList();
            } else {
                ServerLog.info(0, "<search> get " + hits.length + " records for " + keyString);
            }

            List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < hits.length; i++) {
                SearchHit searchHits = hits[i];
                Map<String, Object> map = new HashMap<String, Object>();
                for (String returnFieldName : returnFields){
                    String key = (String) searchHits.getSource().get(returnFieldName);
                    if (StringUtil.isEmpty(key)) {
                        map.put(returnFieldName, key);
                    }
                }
                if (searchHits.getSource() != null){
                    results.add(searchHits.getSource());
                }
            }
            ServerLog.info(0, "<search> final return " + results.toString() + " records for " + keyString);
            return results;
        } else {
            ServerLog.info(0, "<search> response null or no hits");
        }

        return Collections.emptyList();
    }

    public static List<ObjectId> search(String keyString, List<String> candidateFields, String returnFieldName, int offset, int limit, String... indexType) {
        return search(keyString, candidateFields, returnFieldName, null, null, offset, limit, indexType);
    }
}
