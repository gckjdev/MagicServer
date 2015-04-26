package com.orange.game.model.manager.verification;

import com.mongodb.BasicDBObject;
import com.orange.common.utils.RandomUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.service.mail.MailService;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-19
 * Time: 下午5:17
 * To change this template use File | Settings | File Templates.
 */
public class UserVerificationManager extends CommonManager {

    public static final int TYPE_EMAIL = 1;
    public static final int TYPE_SMS = 2;

    private static final int STATUS_NOT_VERIFIED = 0;
    private static final int STATUS_VERIFIED = 1;
    private static final int STATUS_VERIFYING = 2;

    public static final Logger log = Logger.getLogger(UserVerificationManager.class.getName());


    private static UserVerificationManager ourInstance = new UserVerificationManager();

    public static UserVerificationManager getInstance() {
        return ourInstance;
    }

    private UserVerificationManager() {
    }

    public int sendVerficationRequest(String userId, String email, int type) {


        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_EMAIL, email);

        User user = UserManager.updateUserByDBObject(DBService.getInstance().getMongoDBClient(), userId, obj); //UserManager.findPublicUserInfoByUserId(userId);
        if (user == null){
            log.info("<verifyUser> but user not found");
            return ErrorCode.ERROR_USERID_NOT_FOUND;
        }

        if (user.isVerified(email, type)){
            log.info("<verifyUser> but user "+user.getNickName()+" already verified");
            return ErrorCode.ERROR_USER_ALREADY_VERIFIED;
        }

        String code;
        if (!user.hasValidVerifyCode(email, type)){
            code = createAndSetUserVerifyCode(userId, email, type);
            log.info("<verifyUser> try to create code for user "+user.getNickName() +", code "+code);
        }
        else{
            code = user.getVerificationCode(type, email);
            log.info("<verifyUser> but user "+user.getNickName()+" already has code "+code);
        }

        if (StringUtil.isEmpty(code)){
            return ErrorCode.ERROR_USER_CREATE_VERIFY_CODE;
        }

        // send code
        String message = createVerifyMessageBody(user, code, type, email);
        String subject = createVerifyMessageSubject(user, code, type, email);

        return MailService.getInstance().sendEmail(email, subject, message);
    }

    public int verifyUser(String userId, String verifyCode, int type, String email) {

        User user = UserManager.findPublicUserInfoByUserId(userId);
        if (user == null){
            log.info("<verifyUser> but user not found");
            return ErrorCode.ERROR_USERID_NOT_FOUND;
        }

        if (user.isVerified(email, type)){
            log.info("<verifyUser> but user "+user.getNickName()+" already verified");
            return 0;
        }

        String code = user.getVerificationCode(type, email);
        if (code == null){
            log.info("<verifyUser> but user "+user.getNickName()+" code is null");
            return ErrorCode.ERROR_USER_VERIFYCODE_NULL;
        }

        int result = user.verfiyCode(code, type, email);
        if (result != ErrorCode.ERROR_SUCCESS){
            return result;
        }

        log.info("<verifyUser> success! user "+user.getNickName()+" code "+code);

        // update user verfication status
        result = updateUserVerficationStatusOK(user.getUserId(), type);
        return result;
    }


    public int sendPassword(String userId, int type, String email) {

        User user = UserManager.findPublicUserInfoByUserId(userId);
        if (user == null){
            log.info("<sendPassword> but user not found");
            return ErrorCode.ERROR_USERID_NOT_FOUND;
        }

        if (type != TYPE_EMAIL){
            log.info("<sendPassword> but type "+type+" not supported");
            return ErrorCode.ERROR_SEND_TYPE_NOT_SUPPORT;
        }

        String newPassword = null;
        if (!StringUtil.isEmpty(user.getResetPassword()) && !user.needCreateNewPassword()){
            newPassword = user.getResetPassword();
        }
        else{
            newPassword = user.createPassword();
        }

        createAndSetUserPassword(user, newPassword);

        if (StringUtil.isEmpty(newPassword)){
            log.info("<sendPassword> but fail to create password");
            return ErrorCode.ERROR_USER_CREATE_NEW_PASSWORD;
        }

        log.info("<sendPassword> create new password "+newPassword);

        // send code
        String message = createSendPasswordMessageBody(user, newPassword, type, email);
        String subject = createSendPasswordMessageSubject(user, newPassword, type, email);

        int resultCode = MailService.getInstance().sendEmail(email, subject, message);

        String msg = String.format("【温馨提示】你尝试重置了你的密码到邮箱%s\n\n请注意如果未找到重置密码邮件，请到邮件的垃圾箱检查\n" +
                "\n如未找到可尝试再次重置，或者联系小吉客服解决", email);
        MessageManager.sendSystemMessage(mongoClient, user.getUserId(), msg, DBConstants.APPID_DRAW, false);

        return resultCode;

    }

    private String createSendPasswordMessageSubject(User user, String newPassword, int type, String email) {

        String subject = "[小吉] 密码重置邮件";
        return subject;
    }

    private String createSendPasswordMessageBody(User user, String newPassword, int type, String email) {

        String body = String.format("%s 你好，你申请了重置你的密码，你的新密码为 %s <br>请妥善保管好你的密码",
                user.getNickName(), newPassword);

        return body;
    }


    private String createVerifyMessageSubject(User user, String code, int type, String email) {

        String subject = "[小吉]验证码邮件";
        return subject;
    }

    private String createVerifyMessageBody(User user, String code, int type, String email) {

        String body = String.format("%s 你好，你申请验证你的邮件地址，如果这个是你的邮件地址，你的验证码为 %s <br>请在手机客户端输入该验证码",
                user.getNickName(), code);

        return body;
    }




    private String createAndSetUserVerifyCode(String userId, String email, int type) {

        String code = createCode();

        BasicDBObject update = new BasicDBObject();

        String codeField = getVerifyCodeFieldName(type);
        String statusField = getVerifyStatusFieldName(type);

        update.put(codeField, code);
        update.put(statusField, STATUS_VERIFYING);

        User user = UserManager.updateUserByDBObject(DBService.getInstance().getMongoDBClient(), userId, update);
        if (user == null){
            return null;
        }

        return code;
    }

    private int updateUserVerficationStatusOK(String userId, int type) {

        BasicDBObject update = new BasicDBObject();

        String statusField = getVerifyStatusFieldName(type);
        update.put(statusField, STATUS_VERIFIED);

        User user = UserManager.updateUserByDBObject(DBService.getInstance().getMongoDBClient(), userId, update);
        if (user == null){
            return ErrorCode.ERROR_USER_UPDATE_VERFICATION_STATUS;
        }

        return 0;
    }


    private String createAndSetUserPassword(User user, String password) {

        String encryptPassword = user.encryptPassword(password);
        BasicDBObject update = new BasicDBObject();
        update.put(DBConstants.F_PASSWORD, encryptPassword);
        update.put(DBConstants.F_RESET_PASSWORD, password);
        update.put(DBConstants.F_RESET_PASSWORD_DATE, new Date());

        User updatedUser = UserManager.updateUserByDBObject(DBService.getInstance().getMongoDBClient(), user.getUserId(), update);
        if (updatedUser == null){
            return null;
        }

        return password;
    }


    private String createCode() {
        String code = String.valueOf(RandomUtil.random(100000, 999999));
        return code;
    }

    public String getVerifyStatusFieldName(int type) {
        if (type == TYPE_EMAIL)
            return DBConstants.F_EMAIL_VERIFY_STATUS;

        return null;
    }

    public boolean isVerified(int value) {

        if (value == STATUS_VERIFIED){
            return true;
        }

        return false;
    }

    public String getVerifyCodeFieldName(int type) {
        if (type == TYPE_EMAIL)
            return DBConstants.F_EMAIL_VERIFY_CODE;

        return null;
    }
}
