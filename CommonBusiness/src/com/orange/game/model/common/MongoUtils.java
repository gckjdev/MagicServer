package com.orange.game.model.common;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.model.dao.CommonData;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-29
 * Time: 上午10:44
 * To change this template use File | Settings | File Templates.
 */
public class MongoUtils {

    static Logger log = Logger.getLogger(MongoUtils.class.getName());

    public static <T extends CommonData> List<T> findAndMakeList(MongoDBClient mongoDBClient, String tableName, DBObject query,
                                                          DBObject returnFields, DBObject orderBy, int offset, int limit, Class<T> clazz) {

        DBCursor cursor = mongoDBClient.find(tableName, query, returnFields, orderBy, offset, limit);
        if (cursor != null) {
            List<T> list = new ArrayList<T>();
            while (cursor.hasNext()) {
                DBObject obj = cursor.next();
                try {
                    T instance = clazz.newInstance();
                    Constructor<?>[] cst = clazz.getConstructors();
                    instance.setDbObject(obj);
                    list.add(instance);
                } catch (InstantiationException e) {
                    log.error("<findAndMakeList> catch InstantiationException ="+e.toString(), e);
                } catch (Exception e) {
                    log.error("<findAndMakeList> catch exception ="+e.toString(), e);
                }
            }
            cursor.close();
            return list;
        }
        return Collections.emptyList();
    }

}
