package com.orange.game.model.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.orange.common.utils.StringUtil;
import com.orange.game.model.service.DBService;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.App;
import com.orange.game.model.dao.Bulletin;

public class BulletinManager extends CommonManager {

	public static List<Bulletin> getLatestBulletins(MongoDBClient dbClient, String appId, String gameId,
			String userId, String lastBulletinId, int offset, int limit) {
		
		if ( !App.isValidAppId(appId)) {
			ServerLog.info(0, "<BulletinManager> Invalid APP ID");
			return null;
		}
		
		if ( !App.isValidGameId(gameId)) {
			ServerLog.info(0, "<BulletinManager> Invalid game ID");
			return null;
		}
		
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_GAME_ID, gameId);

		if ( lastBulletinId != null && !lastBulletinId.isEmpty()) {
			if (ObjectId.isValid(lastBulletinId)) {
				DBObject gtDbObject = new BasicDBObject();
				gtDbObject.put("$gt", new ObjectId(lastBulletinId));
				query.put(DBConstants.F_OBJECT_ID, gtDbObject);
			}
		}
		
		// Date class is substituted by Calendar, but mongodb recognize
		// only Date, so we have to use it. :p
		long now = System.currentTimeMillis();
		long thirtyDaysAgo = now - 1L * 30 * 24 * 60 * 60 * 1000;
		Date upperBoundDate = new Date(thirtyDaysAgo); 
		DBObject gtObject = new BasicDBObject();
		gtObject.put("$gt", upperBoundDate);
		query.put(DBConstants.F_DATE, gtObject);
		
		return getBulletinList(dbClient, query,  offset, limit);
	}

	private static List<Bulletin> getBulletinList(MongoDBClient dbClient,
			DBObject query, int offset, int limit) {

		DBObject orderBy = new BasicDBObject();
		orderBy.put("_id", -1); // 最新的在最前
		
		DBCursor cursor = dbClient.find(DBConstants.T_BULLETIN, query,
				 orderBy, offset, limit);
		
		if (cursor != null) {
			List<Bulletin> bulletins = new ArrayList<Bulletin>();
			while (cursor.hasNext()) {
				DBObject object = cursor.next();
				if (object != null) {
					Bulletin bulletin = new Bulletin(object);
					bulletins.add(bulletin);
				}
			}
			cursor.close();
			if ( !bulletins.isEmpty() ) 
				return bulletins;
		}
		return null;
	}

    public static void createBulletin(String text, String gameId, int type, String func, String para){

        if (StringUtil.isEmpty(text) || StringUtil.isEmpty(gameId)){
            log.warn("<createBulletin> but text or gameId is null");
            return;
        }

        Bulletin bulletin = new Bulletin();

        bulletin.put(DBConstants.F_DATE, new Date());
        bulletin.put(DBConstants.F_TYPE, type);
        bulletin.put(DBConstants.F_GAME_ID, gameId);
        bulletin.put(DBConstants.F_FUNCTION, func);
        bulletin.put(DBConstants.F_CONTENT, text);
        bulletin.put(DBConstants.F_FUNC_PARA, para);

        log.info("<createBulletin> bulletin="+bulletin.getDbObject().toString());
        DBService.getInstance().getMongoDBClient().insert(DBConstants.T_BULLETIN, bulletin.getDbObject());
    }

 }
