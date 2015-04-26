package com.orange.game.api.barrage.service.chat;

import com.orange.barrage.service.chat.ChatService;
import com.orange.barrage.service.user.FriendService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 15/4/18.
 */
public class SendChatService extends CommonBarrageService {
    private static SendChatService ourInstance = new SendChatService();

    public static SendChatService getInstance() {
        return ourInstance;
    }

    private SendChatService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        UserProtos.PBChat chat = dataRequest.getSendChatRequest().getChat();

        MessageProtos.PBSendChatResponse.Builder rspBuilder = MessageProtos.PBSendChatResponse.newBuilder();
        int resultCode = ChatService.getInstance().sendChat(chat, rspBuilder);

        responseBuilder.setResultCode(resultCode);
        responseBuilder.setSendChatResponse(rspBuilder.build());
    }
}
