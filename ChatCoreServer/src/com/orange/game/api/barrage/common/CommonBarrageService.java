package com.orange.game.api.barrage.common;

import com.orange.protocol.message.MessageProtos;
import org.apache.log4j.Logger;

/**
 * Created by pipi on 14/12/2.
 */
public abstract class CommonBarrageService {

    public static final Logger log = Logger.getLogger(CommonBarrageService.class.getName());

    public abstract boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder);
    public abstract void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder);
}
