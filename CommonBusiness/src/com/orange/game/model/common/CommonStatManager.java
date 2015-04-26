package com.orange.game.model.common;

import com.mongodb.BasicDBObject;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.service.DBService;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-9-30
 * Time: 上午9:53
 * To change this template use File | Settings | File Templates.
 */
public class CommonStatManager extends CommonManager {

    private static final String STAT_PREFIX = "stat_";



    public CommonStatManager(){
    }

    public void incStat(final String name){

        if (StringUtil.isEmpty(name)){
            return;
        }

        DBService.getInstance().executeDBRequest(1, new Runnable() {
            @Override
            public void run() {


                String tableName = createTableName(name);
                String id = getCurrentStatId();

                log.info("<incStat> "+tableName+", id="+id);

                BasicDBObject query = new BasicDBObject("_id", id);
                BasicDBObject update = new BasicDBObject();
                update.put("$inc", new BasicDBObject(DBConstants.F_STAT_VALUE, 1));
                update.put("$set", new BasicDBObject(DBConstants.F_DATE, new Date()));

                DBService.getInstance().getMongoDBClient().upsertAll(tableName, query, update);

            }



        });
    }

    private String getCurrentStatId() {
        Date now = new Date();
        return DateUtil.dateToChineseStringByFormat(now, "yyyyMMdd");
    }

    private String createTableName(String name) {
        return STAT_PREFIX + name;
    }
}
