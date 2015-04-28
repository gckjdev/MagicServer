package com.orange.barrage.service.kafka;

import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 15/4/28.
 */
public interface KafkaChatConsumerCallbackInterface {

    public void handleChatMessage(String agentId, UserProtos.PBChat pbChat);

}
