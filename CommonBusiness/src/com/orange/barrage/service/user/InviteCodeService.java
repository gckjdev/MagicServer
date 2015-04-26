package com.orange.barrage.service.user;

import com.orange.barrage.model.user.InviteCodeManager;
import com.orange.protocol.message.ErrorProtos;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

import java.util.Set;

/**
 * Created by pipi on 14/12/27.
 */
public class InviteCodeService {
    private static InviteCodeService ourInstance = new InviteCodeService();

    public static InviteCodeService getInstance() {
        return ourInstance;
    }

    private InviteCodeService() {
    }

    public int checkInviteCode(String code, MessageProtos.PBVerifyInviteCodeResponse.Builder builder) {
        return InviteCodeManager.getInstance().checkInviteCode(code);
    }

    public int getNewInviteCode(int count, MessageProtos.PBGetNewInviteCodeResponse.Builder builder) {

        Set<String> codes = InviteCodeManager.getInstance().generateInviteCode(count);
        if (codes == null || codes.size() == 0){
            return ErrorProtos.PBError.ERROR_NO_INVITE_CODE_AVAILABLE_VALUE;
        }

        builder.addAllInviteCodes(codes);
        return 0;
    }

    public int getUserInviteCodeList(String userId, MessageProtos.PBGetUserInviteCodeListResponse.Builder builder) {
        UserProtos.PBUserInviteCodeList userCodes = InviteCodeManager.getInstance().getUserInviteCodeList(userId);
        builder.setCodeList(userCodes);
        return 0;
    }

    public int applyInviteCode(String userId, int count, MessageProtos.PBApplyInviteCodeResponse.Builder builder) {

        // apply codes
        Set<String> codes = InviteCodeManager.getInstance().generateInviteCode(count);
        if (codes == null || codes.size() == 0){
            return ErrorProtos.PBError.ERROR_NO_INVITE_CODE_AVAILABLE_VALUE;
        }

        // update to user DB
        UserProtos.PBUserInviteCodeList userCodes = InviteCodeManager.getInstance().addUserInviteCodes(userId, codes);
        builder.setCodeList(userCodes);
        return 0;
    }

    public int updateUserInviteCodeList(String userId, UserProtos.PBUserInviteCodeList updateList, MessageProtos.PBUpdateInviteCodeResponse.Builder builder) {
        UserProtos.PBUserInviteCodeList userCodes = InviteCodeManager.getInstance().updateUserInviteCodeList(userId, updateList);
        builder.setCodeList(userCodes);
        return 0;
    }

    public void updateInviteCodeStatus(String code, int status) {
        InviteCodeManager.getInstance().updateInviteCodeStatus(code, status);
        return;
    }
}
