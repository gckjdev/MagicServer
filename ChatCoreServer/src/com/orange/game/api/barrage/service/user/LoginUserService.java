package com.orange.game.api.barrage.service.user;

import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.service.user.LoginService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.ErrorProtos;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 14/12/2.
 */
public class LoginUserService extends CommonBarrageService {
    private static LoginUserService ourInstance = new LoginUserService();

    public static LoginUserService getInstance() {
        return ourInstance;
    }

    private LoginUserService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder){
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        MessageProtos.PBLoginUserRequest req = dataRequest.getLoginUserRequest();
        MessageProtos.PBLoginUserResponse.Builder rspBuilder = MessageProtos.PBLoginUserResponse.newBuilder();

        int type = req.getType();
        int resultCode = ErrorProtos.PBError.ERROR_USER_LOGIN_UNKNOWN_TYPE_VALUE;
        switch (type){
            case UserProtos.PBLoginType.LOGIN_XIAOJI_VALUE:
                resultCode = LoginService.getInstance().loginByXiaoji(req.getXiaoji(), req.getPassword(), rspBuilder);
                break;

            case UserProtos.PBLoginType.LOGIN_EMAIL_VALUE:
                resultCode = LoginService.getInstance().loginByEmail(req.getEmail(), req.getPassword(), rspBuilder);
                break;

            case UserProtos.PBLoginType.LOGIN_MOBILE_VALUE:
                resultCode = LoginService.getInstance().loginByMobile(req.getMobile(), req.getPassword(), rspBuilder);
                break;

            case UserProtos.PBLoginType.LOGIN_QQ_VALUE:
                resultCode = LoginService.getInstance().loginBySnsId(BarrageConstants.F_QQ_OPEN_ID,
                        req.getSnsId(),
                        rspBuilder);
                break;

            case UserProtos.PBLoginType.LOGIN_SINA_VALUE:
                resultCode = LoginService.getInstance().loginBySnsId(BarrageConstants.F_SINA_ID,
                        req.getSnsId(),
                        rspBuilder);
                break;

            case UserProtos.PBLoginType.LOGIN_WEIXIN_VALUE:
                resultCode = LoginService.getInstance().loginBySnsId(BarrageConstants.F_WEIXIN_ID,
                        req.getSnsId(), rspBuilder);
                break;

        }

        responseBuilder.setResultCode(resultCode);
        responseBuilder.setLoginUserResponse(rspBuilder.build());
    }


}
