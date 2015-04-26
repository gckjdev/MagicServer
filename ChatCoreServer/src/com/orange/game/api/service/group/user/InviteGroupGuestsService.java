package com.orange.game.api.service.group.user;

import com.orange.common.utils.StringUtil;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupUserManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-7
 * Time: 下午2:53
 * To change this template use File | Settings | File Templates.
 */
public class InviteGroupGuestsService extends CommonGameService {

    Set<String> userIds;
    private String groupId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        setUserIdAndAppId(request);
        if (!checkUserIdAndAppId()) {
            return false;
        }
        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);

        if (!check(groupId, ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY,
                ErrorCode.ERROR_PARAMETER_GROUPID_NULL)) {
            return false;
        }

        String userIdsString = request.getParameter(ServiceConstant.PARA_USERID_LIST);
        if (!StringUtil.isEmpty(userIdsString)) {
            String[] uids = userIdsString.split("\\" + ServiceConstant.MESSAGEID_SEPERATOR);
            if (uids != null && uids.length != 0) {
                userIds = new HashSet<String>();
                Collections.addAll(userIds, uids);
//                for (String uid : uids) {
//                    userIds.add(uid);
//                }
            }
        }

        return true;
    }

    @Override
    public void handleData() {
        if (userIds != null && !userIds.isEmpty()) {
            resultCode = GroupUserManager.inviteGuests(mongoClient, userId, gameId, groupId, userIds);
        }
        byteData = protocolBufferWithErrorCode(resultCode);
    }
}
