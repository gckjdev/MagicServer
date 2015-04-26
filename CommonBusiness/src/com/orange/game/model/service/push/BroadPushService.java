package com.orange.game.model.service.push;

import com.mongodb.BasicDBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.app.DrawApp;
import com.orange.game.model.manager.NotificationService;
import com.orange.game.model.manager.push.BoardpushMessageManager;
import org.apache.log4j.Logger;

/**
 * Created by chaoso on 14-8-18.
 */
public class BroadPushService {
    public static final Logger log = Logger.getLogger(BroadPushService.class.getName());
    private static BroadPushService ourInstance = new BroadPushService();

    public static BroadPushService getInstance() {
        return ourInstance;
    }

    private BroadPushService() {
    }
//        String appId = DBConstants.APPID_DRAW;
//        String token = "7cc732d78623b1ccc9251aa734b83058c11670432d75aaacd2df18470c7e4e25";
//        String message = "test me";
        private BoardpushMessageManager boardpushMessageManager = BoardpushMessageManager.getInstance();
        //根據action來 發送消息給用戶
        //PUSH_MESSAGE_ACTION_REMOVE_ALL_SEND 全部數據清除並且重新發一遍
        //PUSH_MESSAGE_ACTION_RESEND_ALL 全部重新發遍
        //PUSH_MESSAGE_ACTION_PUSH 沒有發的重新發
        //PUSH_MESSAGE_ACTION_START_NEW 重頭開始發一遍
        //PUSH_MESSAGE_ACTION_START_NEW 全部插入表一次
        public void pushMessageToUsersWithAction(int action, String name, String xiaoji){

            BasicDBObject query = null;

            if (!StringUtil.isEmpty(xiaoji) && xiaoji.equalsIgnoreCase("all")){
                query = new BasicDBObject(); // all users
            }
            else{
                query = new BasicDBObject("xiaoji", xiaoji);
            }

//            BasicDBObject query = new BasicDBObject("xiaoji", "139847858");

            log.info("<BroadPushServiece> push all message");
            if(action == (DBConstants.F_PUSH_MESSAGE_ACTION_REMOVE_ALL_SEND)){
                boardpushMessageManager.removeAllData(name);
                if (boardpushMessageManager.insertPushTemporaryTable(name, query)) {
                    boardpushMessageManager.pushMessageAgainWithCondition(name, false);
                }
            }
            else if(action == (DBConstants.F_PUSH_MESSAGE_ACTION_RESEND_ALL)){

                boardpushMessageManager.pushMessageAgainWithCondition(name,false);

            }
            else if(action == (DBConstants.F_PUSH_MESSAGE_ACTION_PUSH)){
                boardpushMessageManager.pushMessageAgainWithCondition(name,true);
            }
            else if(action==(DBConstants.F_PUSH_MESSAGE_ACTION_START_NEW)){
                boardpushMessageManager.removeAllData(name);
                if (boardpushMessageManager.insertPushTemporaryTable(name, query)) {
                    boardpushMessageManager.pushMessageAgainWithCondition(name, false);
                }
            }
            else if(action==(DBConstants.F_PUSH_MESSAGE_ACTION_INSERT_TABLE)){
                boardpushMessageManager.insertPushTemporaryTable(name, query);

            }




        }


}
