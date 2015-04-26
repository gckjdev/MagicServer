package com.orange.game.model.manager.user;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.service.DBService;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-10-23
 * Time: 下午9:52
 * To change this template use File | Settings | File Templates.
 */
public class LoginHistoryManager extends CommonManager {
    private static LoginHistoryManager ourInstance = new LoginHistoryManager();

    public static LoginHistoryManager getInstance() {
        return ourInstance;
    }

    private LoginHistoryManager() {
    }

    public void write(final String userId, String email, final String number, String password, String deviceId, String deviceToken, String deviceOs, String deviceModel, int deviceType) {

        final BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_FOREIGN_USER_ID, userId);
        obj.put(DBConstants.F_EMAIL, email);
        obj.put(DBConstants.F_XIAOJI_NUMBER, number);
        obj.put(DBConstants.F_PASSWORD, password);
        obj.put(DBConstants.F_DEVICEID, deviceId);
        obj.put(DBConstants.F_DEVICETOKEN, deviceToken);
        obj.put(DBConstants.F_DEVICEOS, deviceOs);
        obj.put(DBConstants.F_DEVICEMODEL, deviceModel);
        obj.put(DBConstants.F_DEVICE_TYPE, deviceType);
        obj.put(DBConstants.F_DATE, new Date());

        DBService.getInstance().executeDBRequest(1, new Runnable() {
            @Override
            public void run() {

                User fromUser = UserManager.findUserByUserId(mongoClient, userId);
                User loginUser = UserManager.findUserByXiaojiNumber(number);

                if (fromUser != null){
                    obj.put(DBConstants.F_FROM_USER_NICKNAME, fromUser.getNickName());
                    obj.put(DBConstants.F_FROM_USER_XIAOJI, fromUser.getXiaojiNumber());
                    obj.put(DBConstants.F_FROM_USER_ID, fromUser.getUserId());
                }

                if (loginUser != null){
                    obj.put(DBConstants.F_LOGIN_USER_NICKNAME, loginUser.getNickName());
                    obj.put(DBConstants.F_LOGIN_USER_ID, loginUser.getUserId());
                    obj.put(DBConstants.F_LOGIN_USER_XIAOJI, loginUser.getXiaojiNumber());
                }

                log.info("<writeLoginHistory> " + obj.toString());
                DBService.getInstance().getMongoDBClient().insert(DBConstants.T_LOGIN_HISTORY, obj);
            }
        });

    }
}
