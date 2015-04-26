package com.orange.game.api.service.group.user;

import com.orange.common.utils.StringUtil;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.group.GroupTitle;
import com.orange.game.model.manager.group.GroupUserManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-6
 * Time: 上午10:47
 * To change this template use File | Settings | File Templates.
 */
public class InviteGroupUsersService extends CommonGameService {

    Set<String> userIds;
    private String groupId;
    private int titleId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        setUserIdAndAppId(request);
        if (!checkUserIdAndAppId()) {
            return false;
        }
        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);

        titleId = getIntValueFromRequest(request, ServiceConstant.PARA_TITLE_ID, GroupTitle.MEMBER);

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
            resultCode = GroupUserManager.inviteMembers(mongoClient, userId, gameId, groupId, userIds, titleId);
        }
        byteData = protocolBufferWithErrorCode(resultCode);
    }
}
