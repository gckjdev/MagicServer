package com.orange.barrage.service.chat;

import com.orange.barrage.model.chat.*;
import com.orange.barrage.service.push.PushService;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.model.dao.CommonData;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

import java.util.List;

/**
 * Created by pipi on 15/4/18.
 */
public class ChatService {
    private static ChatService ourInstance = new ChatService();

    public static ChatService getInstance() {
        return ourInstance;
    }

    private ChatService() {
    }

    public int sendChat(UserProtos.PBChat pbChat, MessageProtos.PBSendChatResponse.Builder rspBuilder) {

        // create chat into db
        Chat chat = ChatManager.getInstance().createChat(pbChat);
        String chatId = chat.getChatId();

        if (!pbChat.getFromAgent()) {
            String userId = pbChat.getFromUserId();

            // create user chat index
            UserChatManager.getInstance().insertUserChat(userId, chatId, pbChat.getSource());

            String agentId = chat.getToUserId();
            Agent agent = null;
            if (StringUtil.isEmpty(agentId)) {
                // assign agent
                agent = OnlineAgentManager.getInstance().assignAgent(chat);
                if (agent != null) {
                    // update agent in chat
                    ChatManager.getInstance().updateChatToUser(chat, agent);
                    agentId = agent.getAgentId();
                }
            }

            // increase agent counter
            int counter = UserChatCounterManager.getInstance().incAgentChatCounter(agentId);

            // TODO notify agent
    //        OnlineAgentManager.getInstance().notifyAgentNewMessage(agent, counter);

            // for test, auto reply
            testAgentReply(chat, agent);

        }
        else{
            // sent by agent
            String userId = pbChat.getToUserId();

            // create user chat index
            UserChatManager.getInstance().insertUserChat(userId, chatId, pbChat.getSource());

            // increase user counter
            int counter = UserChatCounterManager.getInstance().incUserChatCounter(userId);

            // notify user
            notifyUser(pbChat.getToUser(), pbChat, counter);
            //        OnlineAgentManager.getInstance().notifyAgentNewMessage(agent, counter);
        }

        rspBuilder.setChat(chat.toProtoBufModel());
        return 0;
    }

    private void notifyUser(UserProtos.PBUser toUser, UserProtos.PBChat pbChat, int counter) {

        int deviceType = toUser.getDeviceType();
        String userId = toUser.getUserId();
        String text = pbChat.getText();

        PushService.getInstance().sendMessage(deviceType, userId, text, counter);

    }

    private void testAgentReply(Chat chat, Agent agent) {


        UserProtos.PBChat.Builder chatBuilder = UserProtos.PBChat.newBuilder();
        UserProtos.PBChat sourceChat = chat.toProtoBufModel();

        chatBuilder.setFromAgent(true);
        chatBuilder.setCreateDate(DateUtil.getCurrentSeconds());

        chatBuilder.setFromUser(agent.toProtoBufModel());
        chatBuilder.setFromUserId(agent.getAgentId());

        if (sourceChat.hasFromUser() && sourceChat.getFromUser() != null) {
            chatBuilder.setToUser(sourceChat.getFromUser());
        }
        chatBuilder.setToUserId(sourceChat.getFromUserId());

        chatBuilder.setChatId("");
        chatBuilder.setText("自动回复 "+sourceChat.getText());
        chatBuilder.setType(UserProtos.PBChatType.TEXT_CHAT_VALUE);
        chatBuilder.setSource(UserProtos.PBChatSource.FROM_AGENT_VALUE);

        sendChat(chatBuilder.build(), MessageProtos.PBSendChatResponse.newBuilder());

    }

    public int getUserChatList(String userId,
                               String offsetChatId,
                               int limit,
                               MessageProtos.PBGetChatListResponse.Builder rspBuilder) {

        List<Chat> list = UserChatManager.getInstance().getUserChat(userId, offsetChatId, limit, false);

        // convert data to pb list
        List<UserProtos.PBChat> pbChatList = CommonData.listToPB(list, null);

        // set response
        if (list != null){
            rspBuilder.addAllChat(pbChatList);
        }

        return 0;
    }
}
