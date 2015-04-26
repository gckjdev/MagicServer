package com.orange.game.model.manager.push;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.utils.DateUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.push.PushMessageInfo;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.manager.NotificationService;
import org.apache.commons.lang.time.DateUtils;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Created by chaoso on 14-8-18.
 */
public class BoardpushMessageManager extends CommonManager {

//    private Logger log = Logger.getLogger(BoardpushMessageManager.class.getName());
    private static BoardpushMessageManager ourInstance = new BoardpushMessageManager();

    public static BoardpushMessageManager getInstance() {
        return ourInstance;
    }

    private BoardpushMessageManager() {
    }

    //静态字段
    private final static String message = "9.55版本上线了！新推出:零基础学画画,闯关分享天天有奖!一键分享绘画过程,让你在微博圈中赚足眼球!尽快更新到最新版本发现更多惊喜吧!";

    //按条件将用户存进中间表 <条件：appList不为空，deviceToken不能为空>
    public boolean insertPushTemporaryTable(String name, BasicDBObject query){

        DBCursor cursor = null;
        if (query.size() == 0){
            cursor = mongoClient.findAll(DBConstants.T_USER);
        }
        else {
            cursor = mongoClient.find(DBConstants.T_USER, query, null, 0, 0);
        }

        if (cursor == null) {
            return false;
        }

        boolean result = true;

        try{
            int totalLoopUserCount = 0;
            int totalInsertCount = 0;
            log.info("<insertPushTemp> loop user start");
            while(true){

                DBObject object = null;
                try {
                    object = cursor.next();
                    if (object == null) {
                        break;
                    }
                }
                catch(Exception e){
                    if (object == null){
                        break;
                    }
                    else{
                        continue;
                    }
                }

                User userInfo = new User(object);

                List<String> userAppList = userInfo.getAppIdList();
                String userId = userInfo.getUserId();
                String xiaoji = userInfo.getXiaojiNumber();
                String nickName = userInfo.getNickName();
                String userDeviceToken = userInfo.getDeviceToken();

                for(String appIds : userAppList){
                    if(appIds.equals(DBConstants.APPID_DRAW)||appIds.equals(DBConstants.APPID_LITTLEGEE)){
                        if(userDeviceToken != null && userDeviceToken.length() > 0){
                            //生成實體類，並且存入數據
                            PushMessageInfo pushMessageInfo = new PushMessageInfo(appIds,userId,xiaoji,nickName,userDeviceToken,null, PushMessageInfo.NOT_SEND);
                            //初始化表或者查找表，並且錄入數據
                            insertPushMessageInfo(name, pushMessageInfo);
                            totalInsertCount ++;
                        }
                    }

                }

                totalLoopUserCount++;
            }
            log.info("<insertPushTemp> loop user end, total "+totalLoopUserCount+" user, "+totalInsertCount+" inserted.");


        } catch (Exception e) {
            log.error("<insertPushTemp> catch exception = " + e.toString(), e);
            result = false;
        }
        cursor.close();

        return result;




    }

    private void insertPushMessageInfo(String name, PushMessageInfo info){

        try {
            String tableName = DBConstants.T_PUSH_MESSAGE_BOARDCAST + name;
            mongoClient.insert(tableName, info.getDbObject());
            log.info("<insertPushMessage> table=" + name + ", info=" + info.getDbObject().toString());
        }
        catch(Exception e){
            log.error("<insertPushMessage> exception table=" + name + ", info=" + info.getDbObject().toString(), e);
        }

    }

    //按照條件發消息，當hasCondition 為true的時候就按照條件判斷是否發送消息，為false時候就全部都發
    public void pushMessageAgainWithCondition(String name,Boolean hasCondition){

        // TODO move condition to query

        String tableName = DBConstants.T_PUSH_MESSAGE_BOARDCAST+name;
        log.info("<pushMessage> hasCondition ="+hasCondition +" tableName="+tableName);
        DBCursor cursor = mongoClient.findAll(tableName);
        if (cursor == null) {
            return;
        }

        int totalSend = 0;
        int totalSucc = 0;
        int totalFail = 0;

        // 推送时间设置在早上9点到晚上23点之间
        int MIN_PUSH_HOUR = 9;
        int MAX_PUSH_HOUR = 23;

        //遍歷
        try{
            while(cursor.hasNext()){
                DBObject object = cursor.next();

                Date now = new Date();
                int hour = DateUtil.getHour(now);
                if (hour > MAX_PUSH_HOUR || hour < MIN_PUSH_HOUR){
                    log.warn("<pushMessage> but hour("+hour+") not in valid push range");
                    break;
                }

                PushMessageInfo pushMessageInfo = new PushMessageInfo(object);
                boolean isPush = ((PushMessageInfo.NOT_SEND)==(pushMessageInfo.getSendStatus())
                        &&(PushMessageInfo.FAIL)==(pushMessageInfo.getSendStatus()));

                if(!hasCondition){
                    isPush = true;
                }

                if(isPush){
                    //發送消息
                    int result = NotificationService.getInstance().sendPushMessage(pushMessageInfo.getAppId(),
                            pushMessageInfo.getToken(), 0, message, "default");

                    int sendStatus = (result == 0) ? PushMessageInfo.SUCCESS : PushMessageInfo.FAIL;
                    updateDateAndStatus(pushMessageInfo.get_id(), name, new Date(), sendStatus);

                    totalSend ++;
                    if (sendStatus == PushMessageInfo.SUCCESS){
                        totalSucc ++;
                    }
                    else{
                        totalFail ++;
                    }
                }
            }

        }catch (Exception e){

            log.error("<pushMessage> catch exception="+e.toString(), e);

        }
        cursor.close();

        log.info("<pushMessage> total="+totalSend+", succ="+totalSucc+", fail="+totalFail);
    }

    public void pushMessageAllUser(String name){
        String tableName = DBConstants.T_PUSH_MESSAGE_BOARDCAST+name;
        DBCursor cursor = mongoClient.findAll(tableName);
        if (cursor == null) {
            return;
        }
        //遍歷
        try{
            while(cursor.hasNext()){
                DBObject object = cursor.next();
                PushMessageInfo pushMessageInfo = new PushMessageInfo(object);

                //發送消息
                NotificationService.getInstance().sendPushMessage(pushMessageInfo.getAppId(),
                        pushMessageInfo.getToken(), 0, message, "default");

                boolean ret = updateDateAndStatus(pushMessageInfo.get_id(),name,new Date(),PushMessageInfo.SUCCESS);
                if(!ret){
                    updateDateAndStatus(pushMessageInfo.get_id(),name,new Date(),PushMessageInfo.FAIL);
                }

            }

        }catch (Exception e){

            log.error("<dbObjectsToDataList> class = " + BoardpushMessageManager.class.getName() + ", has no no-arguments constructor", e);

        }
        cursor.close();




    }

    //update 日期与状态
    private boolean updateDateAndStatus(String _id,String name,Date date,int status){
        //根据_id查找PushMessageInfo的出来
        String tableName = DBConstants.T_PUSH_MESSAGE_BOARDCAST+name;

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(_id));
        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_DATE, date);
        updateValue.put(DBConstants.F_STATUS, status);
        update.put("$set", updateValue);

        log.info("<updateDateAndStatus> query="+query.toString()+", update="+update.toString());
        DBObject reObj = mongoClient.findAndModify(tableName, query, update);
        if(reObj == null){
            return false;
        }
        return true;

    }

    public void removeAllData(String name){
        //直接删除表
        String tableName = DBConstants.T_PUSH_MESSAGE_BOARDCAST+name;
        try{
            log.info("<removeAllData> start name="+tableName);
            mongoClient.dropTable(tableName);
        }catch (Exception e){
            log.error("<removeAllData> but error"+e.toString(), e);
        }


    }




}
