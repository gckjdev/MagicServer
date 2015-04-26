package com.orange.game.api.service.group.user;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.group.GroupUserManager;
import com.orange.game.model.utiils.DataUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-2-10
 * Time: 上午11:58
 * To change this template use File | Settings | File Templates.
 */
public class SetUserGroupMessageNoticeService extends CommonGameService {

    String groupId;
    boolean groupMessageNoticeOn = true;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);
        groupMessageNoticeOn = getBoolValueFromRequest(request, ServiceConstant.PARA_STATUS, true);

        if (StringUtil.isEmpty(groupId)){
            resultCode = ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY;
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {
        GroupUserManager.setUserGroupMessageNotice(groupId, userId, groupMessageNoticeOn);
        User user = UserManager.setUserGroupMessageNotice(userId, groupId, groupMessageNoticeOn);
        if (user == null){
            byteData = DataUtils.protocolBufferErrorCode(ErrorCode.ERROR_USER_NOT_FOUND);
            resultCode = ErrorCode.ERROR_USER_NOT_FOUND;
            return;
        }

        byteData = DataUtils.userToPB(user, appId);
    }
}
