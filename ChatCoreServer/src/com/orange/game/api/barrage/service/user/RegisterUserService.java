package com.orange.game.api.barrage.service.user;

import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.user.InviteCodeManager;
import com.orange.barrage.service.user.LoginService;
import com.orange.barrage.service.user.RegisterService;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.ErrorProtos;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 14/12/2.
 */
public class RegisterUserService  extends CommonBarrageService {
    private static RegisterUserService ourInstance = new RegisterUserService();

    public static RegisterUserService getInstance() {
        return ourInstance;
    }

    private RegisterUserService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        UserProtos.PBUser user = dataRequest.getRegisterUserRequest().getUser();
        MessageProtos.PBRegisterUserRequest req = dataRequest.getRegisterUserRequest();
        MessageProtos.PBRegisterUserResponse.Builder rspBuilder = MessageProtos.PBRegisterUserResponse.newBuilder();

        int resultCode = 0;
        String inviteCode = req.getInviteCode();
        if (inviteCode != null){
            resultCode = InviteCodeManager.getInstance().checkInviteCode(inviteCode);
            if (resultCode != 0){
                // check invite code failure, return
                responseBuilder.setResultCode(resultCode);
                return;
            }
        }

        int type = req.getType();
        resultCode = ErrorProtos.PBError.ERROR_USER_REGISTER_UNKNOWN_TYPE_VALUE;
        switch (type){
            case UserProtos.PBRegisterType.REG_EMAIL_VALUE:
                resultCode = RegisterService.getInstance().registerByEmail(user, rspBuilder);
                break;

            case UserProtos.PBRegisterType.REG_MOBILE_VALUE:
                resultCode = RegisterService.getInstance().registerByMobile(user, rspBuilder);
                break;

            case UserProtos.PBRegisterType.REG_QQ_VALUE:
                resultCode = RegisterService.getInstance().registerBySNS(user, rspBuilder
                        , BarrageConstants.F_QQ_OPEN_ID, user.getQqOpenId());
                break;

            case UserProtos.PBRegisterType.REG_SINA_VALUE:
                resultCode = RegisterService.getInstance().registerBySNS(user, rspBuilder
                        , BarrageConstants.F_SINA_ID, user.getSinaId());
                break;

            case UserProtos.PBRegisterType.REG_WEIXIN_VALUE:
                resultCode = RegisterService.getInstance().registerBySNS(user, rspBuilder
                        , BarrageConstants.F_WEIXIN_ID, user.getWeixinId());
                break;

        }

        responseBuilder.setResultCode(resultCode);
        responseBuilder.setRegisterUserResponse(rspBuilder.build());

        // TODO update user device if needed

        if (resultCode == 0 && !StringUtil.isEmpty(inviteCode)){
            // clear invite code here if needed
            String userId = responseBuilder.getRegisterUserResponse().getUser().getUserId();
            InviteCodeManager.getInstance().useInviteCode(inviteCode, userId);
        }
    }
}
