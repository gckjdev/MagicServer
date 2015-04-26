package com.orange.game.api.service.message;

import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.model.dao.Message;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.message.UserMessageManager;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-7-22
 * Time: 上午9:33
 * To change this template use File | Settings | File Templates.
 */
public class NewGetMessageListService extends GetMessageListService {

    @Override
    public void handleData() {
        // new implementation, use this after 6.9 version is released
        List<Message> messageList = UserMessageManager.getInstance().getUserMessageList(userId, friendUserId, isGroup, offsetMessageId, limit, forward);
        byteData = CommonServiceUtils.userMessageListToProtocolBuffer(messageList, isGroup);
    }
}
