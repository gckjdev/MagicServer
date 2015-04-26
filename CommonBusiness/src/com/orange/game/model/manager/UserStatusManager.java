package com.orange.game.model.manager;

import java.util.Date;

import com.mongodb.DBObject;
import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.stat.ShareStatManager;
import com.orange.game.model.service.DBService;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import redis.clients.jedis.Jedis;

public class UserStatusManager extends CommonManager {

	public static final Logger log = Logger.getLogger(UserStatusManager.class
			.getName());
	
	private static final long EXPIRE_TIME_FOR_REPORT = 20*60*1000;	// 20 minutes

	private static final int STATUS_ONLINE = 0;
	private static final int STATUS_OFFLINE = 1;

	public static void reportUserStatus(final MongoDBClient mongoClient,
                                        final String userId,
                                        final String appId,
                                        final int onlineStatus,
                                        final String deviceModel,
                                        final String deviceToken) {

		// upsert record
        DBService.getInstance().executeDBRequest(2, new Runnable() {
            @Override
            public void run() {
                BasicDBObject query = new BasicDBObject();
                query.put("_id", new ObjectId(userId));
                if (onlineStatus == STATUS_ONLINE){

                    BasicDBObject update = new BasicDBObject();
                    update.put("_id", new ObjectId(userId));
                    update.put(DBConstants.F_APPID, appId);
                    update.put(DBConstants.F_MODIFY_DATE, new Date());
                    update.put(DBConstants.F_STATUS, onlineStatus);
                    update.put(DBConstants.F_EXPIRE_DATE, new Date(System.currentTimeMillis()+EXPIRE_TIME_FOR_REPORT));

                    log.info("<reportUserStatus> online query="+userId+", update="+update.toString());
                    mongoClient.updateOrInsert(DBConstants.T_USER_STATUS, query, update);

                }
                else{
                    log.info("<reportUserStatus> user " + userId + " offline");
                    mongoClient.remove(DBConstants.T_USER_STATUS, query);
                }

                // update last visit date in user table
                BasicDBObject userModifyObj = new BasicDBObject();
                userModifyObj.put(DBConstants.F_VISIT_DATE, new Date());
                DBObject obj = mongoClient.findAndModify(DBConstants.T_USER, query, new BasicDBObject("$set", userModifyObj));

//                UserManager.updateUserByDBObject(mongoClient, userId, userModifyObj);

                if (onlineStatus == STATUS_ONLINE){

                    // write into new user table and active user table
                    if (obj != null){
                        User user = new User(obj);
                        if (!StringUtil.isEmpty(deviceModel)){
                            user.setDeviceModel(deviceModel);
                        }
                        ShareStatManager.writeDailyActiveUser(user);

                        if (!StringUtil.isEmpty(deviceToken)){
                            if (StringUtil.isEmpty(user.getString(DBConstants.F_DEVICETOKEN))){
                                // update directly
                                UserManager.addMainDeviceToken(user.getUserId(), deviceToken);
                            }
                            else if (user.getString(DBConstants.F_DEVICETOKEN).equalsIgnoreCase(deviceToken)){
                                // the same, do nothing
                            }
                            else{
                                String oldDeviceToken = user.getString(DBConstants.F_DEVICETOKEN);

                                // update directly
                                UserManager.replaceMainDeviceToken(user.getUserId(), deviceToken, oldDeviceToken);
                            }
                        }
                    }


                    final String USER_ENTER_APP_COUNT = "stat_user_enter_app";
                    RedisClient.getInstance().execute(new RedisCallable() {
                        @Override
                        public Object call(Jedis jedis) {
                            String dateKey = DateUtil.dateToChineseStringByFormat(new Date(), "yyyyMMdd");
                            log.info("<Redis> inc "+USER_ENTER_APP_COUNT+","+dateKey);
                            jedis.hincrBy(USER_ENTER_APP_COUNT, dateKey, 1);
                            return null;
                        }
                    });
                }
            }
        });


	}
}
