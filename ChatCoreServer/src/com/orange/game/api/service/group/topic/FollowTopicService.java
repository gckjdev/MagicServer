package com.orange.game.api.service.group.topic;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.index.UserTopicIndexManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-23
 * Time: 下午2:55
 * To change this template use File | Settings | File Templates.
 */
public class FollowTopicService extends CommonGameService {

    private String topicId;


    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
        topicId = request.getParameter(ServiceConstant.PARA_POSTID);

        if (!check(topicId, ErrorCode.ERROR_PARAMETER_POSTID_EMPTY,
                ErrorCode.ERROR_PARAMETER_POSTID_NULL)) {
            return false;
        }

        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleData() {
        UserTopicIndexManager.getManagerForFollow().insertIndex(userId, topicId);
        byteData = protocolBufferWithErrorCode(0);
    }
}
