package com.orange.barrage.model.chat;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.barrage.common.CommonModelManager;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.user.User;
import com.orange.common.utils.DateUtil;
import com.orange.game.api.service.ElasticsearchService;
import com.orange.game.model.manager.CommonManager;
import com.orange.protocol.message.UserProtos;
import org.bson.types.ObjectId;

/**
 * Created by pipi on 15/4/18.
 */
public class ChatManager extends CommonModelManager<Chat> {
    private static ChatManager ourInstance = new ChatManager();

    public static ChatManager getInstance() {
        return ourInstance;
    }

    private ChatManager() {
    }

    public Chat createChat(UserProtos.PBChat chat) {

        UserProtos.PBChat.Builder builder = UserProtos.PBChat.newBuilder(chat);

        // set some auto creation data here
        builder.setCreateDate(DateUtil.getCurrentSeconds());

        DBObject obj = Chat.pbToDBObject(builder.build(), true, BarrageConstants.F_CHAT_ID);

        log.info("create chat = "+obj.toString());
        mongoDBClient.insert(BarrageConstants.T_CHAT, obj);

        Chat retObj = new Chat(obj);

        // TODO index in Elastic Search
//        ElasticsearchService.addOrUpdateIndex(retObj, mongoDBClient);
        return retObj;
    }

    @Override
    public String getTableName() {
        return BarrageConstants.T_CHAT;
    }

    @Override
    public Class<Chat> getClazz() {
        return Chat.class;
    }

    public void updateChatToUser(Chat chat, Agent agent) {

        // update in object
        chat.setToUserId(agent.getAgentId());
        chat.setToUser(agent);

        // update DB
        BasicDBObject query = new BasicDBObject("_id", new ObjectId(chat.getChatId()));
        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(BarrageConstants.F_TO_USER, chat.getToUser());
        updateValue.put(BarrageConstants.F_TO_USER_ID, chat.getToUserId());
        update.put("$set", updateValue);
        log.info("<updateChatToUser> chatId="+chat.getChatId()+", toUser="+update.toString());
        mongoDBClient.updateAll(BarrageConstants.T_CHAT, query, update);
    }
}
