package com.orange.game.api.service.message;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.MessageManager;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-7-20
 * Time: 上午11:36
 * To change this template use File | Settings | File Templates.
 */
public class DeleteSingleMessageService extends CommonGameService {

    String targetUserId;
    String messageId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
        messageId = request.getParameter(ServiceConstant.PARA_MESSAGE_ID);

        if (!check(targetUserId, ErrorCode.ERROR_PARAMETER_TARGET_USERID_EMPTY,
                ErrorCode.ERROR_PARAMETER_TARGET_USERID_NULL))
            return false;

        if (!check(messageId, ErrorCode.ERROR_PARAMETER_MESSAGEID_EMPTY,
                ErrorCode.ERROR_PARAMETER_MESSAGEID_NULL))
            return false;

        return true;
    }

    @Override
    public void handleData() {

        // check whether message is GROUP message, if so, cannot be deleted
        if (MessageManager.isGroupMessage(messageId)){
            resultCode = ErrorCode.ERROR_CANNOT_DELETE_GROUP_MESSAGE;
            log.warn("delete message but message "+messageId+" is GROUP message, cannot delete");
            return;
        }

        List<String> messageIdList = new ArrayList<String>();
        messageIdList.add(messageId);
        MessageManager.deleteMessage(mongoClient, userId, targetUserId, messageIdList);
    }

    @Override
    public String toString() {
        return "DeleteSingleMessageService{" +
                "targetUserId='" + targetUserId + '\'' +
                ", messageId='" + messageId + '\'' +
                '}';
    }
}
