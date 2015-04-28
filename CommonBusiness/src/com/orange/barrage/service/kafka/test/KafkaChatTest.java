package com.orange.barrage.service.kafka.test;

import com.orange.barrage.service.kafka.KafkaChatConsumerCallbackInterface;
import com.orange.barrage.service.kafka.KafkaChatConsumerManager;
import com.orange.protocol.message.UserProtos;
import org.apache.log4j.Logger;

/**
 * Created by pipi on 15/4/28.
 */
public class KafkaChatTest {

    protected static Logger log = Logger.getLogger(KafkaChatTest.class.getName());

    public static void main(String[] arg) {

        KafkaChatConsumerManager.getInstance().addAgent("5538db97d4c6528de7f00e07", new KafkaChatConsumerCallbackInterface() {
            @Override
            public void handleChatMessage(String agentId, UserProtos.PBChat pbChat) {
                if (pbChat != null) {
                    System.out.println("<handleChatMessage> agentId=" + agentId + ", chat=" + pbChat.toString());
                }
                else{
                    System.out.println("<handleChatMessage> agentId=" + agentId);
                }
            }
        });
    }
}
