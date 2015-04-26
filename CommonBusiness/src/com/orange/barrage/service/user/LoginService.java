package com.orange.barrage.service.user;

import com.orange.barrage.common.CommonModelService;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.user.User;
import com.orange.barrage.model.user.UserManager;
import com.orange.common.utils.StringUtil;
import com.orange.protocol.message.ErrorProtos;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 14/12/2.
 */
public class LoginService extends CommonModelService {

    private static LoginService ourInstance = new LoginService();

    public static LoginService getInstance() {
        return ourInstance;
    }

    private LoginService() {
    }

    public int loginByXiaoji(String xiaoji, String password, MessageProtos.PBLoginUserResponse.Builder rspBuilder) {

        if (StringUtil.isEmpty(xiaoji) ||
                StringUtil.isEmpty(password)){
            return ErrorProtos.PBError.ERROR_USER_LOGIN_INFO_EMPTY_VALUE;
        }



        return 0;
    }

    public int loginByEmail(String email, String password, MessageProtos.PBLoginUserResponse.Builder rspBuilder) {

        if (StringUtil.isEmpty(email) || StringUtil.isEmpty(password)){
            return ErrorProtos.PBError.ERROR_USER_LOGIN_INFO_EMPTY_VALUE;
        }

        // find user
        User user = UserManager.getInstance().findUserByEmail(email);
        if (user == null){
            log.warn("<loginByEmail> but user not found, login info=" + email);
            return ErrorProtos.PBError.ERROR_USER_NOT_FOUND_VALUE;
        }

        // check password
        return checkUserPassword(user, password, rspBuilder);
    }

    private int checkUserPassword(User user, String password, MessageProtos.PBLoginUserResponse.Builder rspBuilder) {

        int resultCode = user.checkPassword(password) ? 0 : ErrorProtos.PBError.ERROR_PASSWORD_INVALID_VALUE;
        if (resultCode != 0){
            return resultCode;
        }

        rspBuilder.setUser(user.toProtoBufModel());
        return 0;
    }

    public int loginByMobile(String mobile, String password, MessageProtos.PBLoginUserResponse.Builder rspBuilder) {
        if (StringUtil.isEmpty(mobile) || StringUtil.isEmpty(password)){
            return ErrorProtos.PBError.ERROR_USER_LOGIN_INFO_EMPTY_VALUE;
        }

        // find user
        User user = UserManager.getInstance().findUserByMobile(mobile);
        if (user == null){
            log.warn("<loginByMobile> but user not found, login info=" + mobile);
            return ErrorProtos.PBError.ERROR_USER_NOT_FOUND_VALUE;
        }

        return checkUserPassword(user, password, rspBuilder);
    }

    public int loginBySnsId(String snsFieldName, String snsId, MessageProtos.PBLoginUserResponse.Builder rspBuilder) {
        if (StringUtil.isEmpty(snsId)){
            return ErrorProtos.PBError.ERROR_USER_LOGIN_INFO_EMPTY_VALUE;
        }

        // find user
        User user = UserManager.getInstance().findUserBySnsId(snsFieldName, snsId);
        if (user == null){
            log.warn("<loginBySnsId> but user not found, login info=" + snsFieldName+", "+ snsId);
            return ErrorProtos.PBError.ERROR_USER_NOT_FOUND_VALUE;
        }

        rspBuilder.setUser(user.toProtoBufModel());

        // TODO update SNS credential info
        return 0;
    }
}
