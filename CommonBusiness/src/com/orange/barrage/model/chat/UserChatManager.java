package com.orange.barrage.model.chat;

import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.feed.Feed;
import com.orange.common.utils.StringUtil;
import com.orange.game.model.common.CommonCassandraIdListManager;

import java.util.List;

/**
 * Created by pipi on 15/4/18.
 */
public class UserChatManager extends CommonCassandraIdListManager<Chat> {
    private static UserChatManager ourInstance = new UserChatManager();

    public static UserChatManager getInstance() {
        return ourInstance;
    }

    private UserChatManager() {
        super(BarrageConstants.T_USER_CHAT, BarrageConstants.T_CHAT, Chat.class);
    }

    public void insertUserChat(String userId, String chatId, int source){
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(chatId)){
            log.info("<insertUserChat> but userId or chatId is EMPTY/NULL");
            return;
        }

        String key = userId;
        insertIndex(key, chatId, String.valueOf(source));
    }

    public void deleteUserChat(String userId, String chatId){
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(chatId)){
            log.info("<deleteUserChat> but userId or chatId is EMPTY/NULL");
            return;
        }

        String key = userId;
        log.info("<deleteUserFeed> key="+key+", chatId="+chatId);
        deleteIndex(key, chatId);
    }

    public List<Chat> getUserChat(String userId, String offsetChatId, int limit, boolean forward){
        String key = userId;

        String startOffsetId = null;
        String endOffsetId = null;

        if (forward)
            endOffsetId = offsetChatId;
        else
            startOffsetId = offsetChatId;

        return getList(key, startOffsetId, endOffsetId, limit, null, 0, null);
    }
}
