package com.orange.game.api.service.bbs;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSAction;
import com.orange.game.model.manager.bbs.BBSActionManager;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.bbs.BBSUserActionManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

public class GetBBSActionListService extends CommonGameService {
    String postId;
    String targetUid;
    int deviceType;
    // support or comment
    int actionType;
    int offset;
    int limit;
    private int mode;

    @Override
    public void handleData() {

        List<BBSAction> actionList = null;
        if (!StringUtil.isEmpty(targetUid)) {
            actionList = BBSUserActionManager.getInstance(mode).getList(targetUid, offset, limit);
        } else if (actionType == BBSAction.ActionTypeComment) {
            actionList = BBSActionManager.commentManagerInstance(mode).getList(postId, offset, limit);
        } else if (actionType == BBSAction.ActionTypeSupport) {
            actionList = BBSActionManager.supportManagerInstance(mode).getList(postId, offset, limit);
        } else {
            actionList = Collections.emptyList();
        }
        log.info("<GetBBSActionList> count = " + actionList.size());
        byteData = CommonServiceUtils.bbsActionListToProto(actionList);
    }

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);

        deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);


        // get target uid
        targetUid = request.getParameter(ServiceConstant.PARA_TARGETUSERID);

        // get board post
        postId = request.getParameter(ServiceConstant.PARA_POSTID);


        if (StringUtil.isEmpty(targetUid) && StringUtil.isEmpty(postId)) {
            resultCode = ErrorCode.ERROR_GET_ME_BBSACTION_LIST;
            return false;
        }

        actionType = getIntValueFromRequest(request,
                ServiceConstant.PARA_ACTION_TYPE, BBSAction.ActionTypeNO);

        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT,
                ServiceConstant.DEFAULT_MAX_COUNT);

        return true;
    }

}
