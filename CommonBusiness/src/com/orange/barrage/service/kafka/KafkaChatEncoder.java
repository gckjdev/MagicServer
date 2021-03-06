package com.orange.barrage.service.kafka;

import com.orange.protocol.message.UserProtos;
import kafka.message.Message;
import kafka.utils.VerifiableProperties;

/**
 * Created by pipi on 15/4/28.
 */
public class KafkaChatEncoder implements kafka.serializer.Encoder<UserProtos.PBChat> {

    public KafkaChatEncoder(VerifiableProperties verifiableProperties) {
        /* This constructor must be present for successful compile. */
    }


    @Override
    public byte[] toBytes(UserProtos.PBChat pbChat) {
        return pbChat.toByteArray();
    }

}
