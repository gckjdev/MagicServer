package com.orange.barrage.service.chat;

import com.orange.barrage.model.chat.*;
import com.orange.barrage.service.kafka.KafkaChatProducer;
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

            // write agent user history
            AgentUserHistoryManager.writeAgentUserHistory(agent.getAgentId(), pbChat.getFromUserId());

            // notify agent
//            KafkaChatProducer.getInstance().sendChat(chat.toProtoBufModel(), agent);


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

    public int autoSendType = 0;

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

        int type = ( autoSendType++ ) % 3;
        switch (type) {
            case UserProtos.PBChatType.PICTURE_CHAT_VALUE:
                chatBuilder.setImage("http://e.hiphotos.baidu.com/image/pic/item/e1fe9925bc315c60103d2bee8fb1cb1349547702.jpg");
                break;
            case UserProtos.PBChatType.TEXT_CHAT_VALUE:
                chatBuilder.setText("自动回复 " + sourceChat.getText());
                break;
            case UserProtos.PBChatType.VOICE_CHAT_VALUE:
                chatBuilder.setVoice("http://gckjdev.qiniudn.com/chat/voice/audio/20150427/8c11f6aee71b4587ab4a585dda0d1bfd.wav");
                chatBuilder.setDuration(2);
                break;
        }
        chatBuilder.setType(type);
        chatBuilder.setSource(UserProtos.PBChatSource.FROM_AGENT_VALUE);

//        sendChat(chatBuilder.build(), MessageProtos.PBSendChatResponse.newBuilder());

        ChatHttpClientAPI.getInstance().sendChat(chatBuilder.build(), agent.getAgentId());
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
