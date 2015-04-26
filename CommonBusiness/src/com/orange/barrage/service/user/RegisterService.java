package com.orange.barrage.service.user;

import com.googlecode.protobuf.format.JsonFormat;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.orange.barrage.common.CommonModelService;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.user.User;
import com.orange.barrage.model.user.UserManager;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.protocol.message.ErrorProtos;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;
import org.bson.types.ObjectId;

/**
 * Created by pipi on 14/12/2.
 */
public class RegisterService extends CommonModelService {
    private static RegisterService ourInstance = new RegisterService();

    public static RegisterService getInstance() {
        return ourInstance;
    }

    private RegisterService() {

    }

    public int registerByEmail(UserProtos.PBUser pbUser, MessageProtos.PBRegisterUserResponse.Builder rspBuilder) {

        String email = pbUser.getEmail();
        if (StringUtil.isEmpty(email)){
            return ErrorProtos.PBError.ERROR_EMAIL_EMPTY_VALUE;
        }

        User user = UserManager.getInstance().findUserByEmail(pbUser.getEmail());
        if (user != null){
            // user exist
            String password = pbUser.getPassword();
            if (!StringUtil.isEmpty(password) && user.checkPassword(password)) {
                log.warn("<registerByEmail> user exist & password verified, return directly, user=" + user.toString());
                rspBuilder.setUser(user.toProtoBufModel());
                rspBuilder.setIsUserExist(true);
                return 0;
            }
            else {
                log.warn("<registerByEmail> but user email registered, user=" + user.toString());
                return ErrorProtos.PBError.ERROR_EMAIL_REGISTERED_VALUE;
            }

//            // user exist
//            log.warn("<registerByEmail> but user exist, failure, user=" + user.toString());
//            return ErrorProtos.PBError.ERROR_EMAIL_REGISTERED_VALUE;
        }

        // user not found, create user by email directly
        User retUser = UserManager.getInstance().createNewUser(pbUser);

        // set response builder
        if (retUser == null){
            return ErrorProtos.PBError.ERROR_UNKNOWN_VALUE;
        }

        rspBuilder.setUser(retUser.toProtoBufModel());
        return 0;
    }

    public int registerByMobile(UserProtos.PBUser pbUser, MessageProtos.PBRegisterUserResponse.Builder rspBuilder) {
        String mobile = pbUser.getMobile();
        if (StringUtil.isEmpty(mobile)){
            return ErrorProtos.PBError.ERROR_MOBILE_EMPTY_VALUE;
        }

        User user = UserManager.getInstance().findUserByMobile(pbUser.getMobile());
        String password = pbUser.getPassword();
        if (user != null){
            // user exist
            if (!StringUtil.isEmpty(password) && user.checkPassword(password)) {
                log.warn("<registerByMobile> user exist & password verified, return directly, user=" + user.toString());
                rspBuilder.setUser(user.toProtoBufModel());
                rspBuilder.setIsUserExist(true);
                return 0;
            }
            else {
                log.warn("<registerByMobile> but user mobile registered, user=" + user.toString());
                return ErrorProtos.PBError.ERROR_MOBILE_EXIST_VALUE;
            }
        }

        // user not found, create user by email directly
        User retUser = UserManager.getInstance().createNewUser(pbUser);

        // set response builder
        if (retUser == null){
            return ErrorProtos.PBError.ERROR_UNKNOWN_VALUE;
        }

        rspBuilder.setUser(retUser.toProtoBufModel());
        return 0;
    }

    public int registerBySNS(UserProtos.PBUser pbUser, MessageProtos.PBRegisterUserResponse.Builder rspBuilder,
                             String snsFieldName, String snsId) {

        if (StringUtil.isEmpty(snsId)){
            return ErrorProtos.PBError.ERROR_SNSID_EMPTY_VALUE;
        }

        User user = UserManager.getInstance().findUserBySnsId(snsFieldName, snsId);
        if (user != null){
            // user exist
            log.warn("<registerBySNS> but user exist, return directly, user=" + user.toString());
            rspBuilder.setUser(user.toProtoBufModel());
            rspBuilder.setIsUserExist(true);

            // TODO update sns credential info in DB

            return 0;
        }

        // user not found, create user by email directly
        User retUser = UserManager.getInstance().createNewUser(pbUser);

        // set response builder
        if (retUser == null){
            return ErrorProtos.PBError.ERROR_UNKNOWN_VALUE;
        }

        rspBuilder.setUser(retUser.toProtoBufModel());
        return 0;

    }
}
