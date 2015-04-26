package com.orange.barrage.model.feed;

import com.googlecode.protobuf.format.JsonFormat;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.orange.barrage.common.CommonModelManager;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.ErrorProtos;
import com.orange.protocol.message.MessageProtos;
import org.bson.types.ObjectId;

/**
 * Created by pipi on 14/12/8.
 */
public class FeedManager extends CommonModelManager<Feed> {
    private static FeedManager ourInstance = new FeedManager();

    public static FeedManager getInstance() {
        return ourInstance;
    }

    private FeedManager() {
    }



    @Override
    public String getTableName() {
        return BarrageConstants.T_FEED;
    }

    @Override
    public Class getClazz() {
        return Feed.class;
    }
}
