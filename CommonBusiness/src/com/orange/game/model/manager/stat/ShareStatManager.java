package com.orange.game.model.manager.stat;

import com.mongodb.BasicDBObject;
import com.orange.common.utils.DateUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonStatManager;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserAction;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-9-30
 * Time: 上午10:20
 * To change this template use File | Settings | File Templates.
 */
public class ShareStatManager extends CommonStatManager {

    public static final String OPUS = "opus";
    public static final String OPUS_COMMENT = "opus_comment";
    public static final String OPUS_GUESS = "opus_guess";
    public static final String NEW_USER = "new_user";
    public static final String NEW_USER_FAILURE = "new_user_failure";
    public static final String POST = "post";

    public static final String GROUP_TOPIC = "group_topic";
    public static final String GROUP_ACTION = "group_action";

    public static final String OPUS_FAVORITE = "opus_favorite";
    public static final String OPUS_PLAY = "opus_play";
    public static final String OPUS_FLOWER = "opus_flower";
    public static final String OPUS_CONTEST_COMMENT = "opus_contest_comment";
    public static final String POST_ACTION = "post_action";

    private static ShareStatManager ourInstance = new ShareStatManager();

    public static ShareStatManager getInstance() {
        return ourInstance;
    }

    private ShareStatManager() {
    }

    public static void writeDailyActive(String tableName, String key, String id, String deviceModel) {

        BasicDBObject query = new BasicDBObject("_id", key);
        BasicDBObject updateValue = new BasicDBObject();
        if (deviceModel != null){
            if (deviceModel.toLowerCase().contains("ipad")){
                deviceModel = "iPad";
            }
            else if (deviceModel.toLowerCase().contains("iphone")){
                deviceModel = "iPhone";
            }
            else if (deviceModel.toLowerCase().contains("ipod")){
                deviceModel = "iPhone";
            }

            String idKey = deviceModel.toLowerCase()+"_ids";
            updateValue.put("$addToSet", new BasicDBObject(idKey, id));

            String incCountKey = deviceModel.toLowerCase() + "_count";
            updateValue.put("$inc", new BasicDBObject(incCountKey, 1));

            log.info("<writeDailyActive> "+tableName+" query="+query + " update="+updateValue.toString());
            mongoClient.upsertAll(tableName, query, updateValue);
        }
        else{
            log.warn("<writeDailyActive> "+tableName+" key="+key+", deviceModel is null!");
        }
    }

    public static void writeDailyActiveUser(User user) {

        if (user == null){
            return;
        }

        String tableName = DBConstants.T_STAT_DAILY_ACTIVE_USER;
        String key = DateUtil.dateToChineseStringByFormat(new Date(), "yyyyMMdd");
        String deviceModel = user.getDeviceModel();

        writeDailyActive(tableName, key, user.getUserId(), deviceModel);
    }

    public static void writeDailyNewUser(User user) {

        if (user == null){
            return;
        }

        String tableName = DBConstants.T_STAT_DAILY_NEW_USER;
        String key = DateUtil.dateToChineseStringByFormat(new Date(), "yyyyMMdd");
        String deviceModel = user.getDeviceModel();

        writeDailyNew(tableName, key, deviceModel);
    }

    public static void writeDailyNewOpus(UserAction opus, User user) {

        if (user == null || opus == null){
            return;
        }

        String tableName;
        String key = DateUtil.dateToChineseStringByFormat(new Date(), "yyyyMMdd");
        String deviceModel = opus.getDeviceModel() !=null ? opus.getDeviceModel() : user.getDeviceModel();

        if (Math.abs(user.getCreateDate().getTime() - System.currentTimeMillis()) < 24*60*60*1000){
            // user is registered inside 24 hours, take as new
            tableName = DBConstants.T_STAT_DAILY_NEW_OPUS_BY_NEW_USER;
        }
        else{
            tableName = DBConstants.T_STAT_DAILY_NEW_OPUS_BY_OLD_USER;
        }

        writeDailyNew(tableName, key, deviceModel);
    }

    public static void writeDailyNew(String tableName, String key, String deviceModel) {

        BasicDBObject query = new BasicDBObject("_id", key);
        BasicDBObject updateValue = new BasicDBObject();
        if (deviceModel != null){
            if (deviceModel.toLowerCase().contains("ipad")){
                deviceModel = "iPad";
            }
            else if (deviceModel.toLowerCase().contains("iphone")){
                deviceModel = "iPhone";
            }
            else if (deviceModel.toLowerCase().contains("ipod")){
                deviceModel = "iPhone";
            }

            String incCountKey = deviceModel.toLowerCase() + "_count";
            updateValue.put("$inc", new BasicDBObject(incCountKey, 1));

            log.info("<writeDailyNew> "+tableName+" query="+query + " update="+updateValue.toString());
            mongoClient.upsertAll(tableName, query, updateValue);
        }
        else{
            log.warn("<writeDailyNew> "+tableName+" key="+key+", deviceModel is null!");
        }
    }
}
