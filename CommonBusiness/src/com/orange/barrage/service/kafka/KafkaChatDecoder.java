package com.orange.barrage.service.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.orange.protocol.message.UserProtos;
import kafka.serializer.Decoder;
import kafka.utils.VerifiableProperties;
import org.apache.log4j.Logger;

/**
 * Created by pipi on 15/4/28.
 */
public class KafkaChatDecoder  implements Decoder<UserProtos.PBChat> {

    protected static Logger log = Logger.getLogger(KafkaChatDecoder.class.getName());

    @Override
    public UserProtos.PBChat fromBytes(byte[] bytes) {
        try {
            return UserProtos.PBChat.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("KafkaChatDecoder decode execption="+e.toString(), e);
            return null;
        }
    }
}
