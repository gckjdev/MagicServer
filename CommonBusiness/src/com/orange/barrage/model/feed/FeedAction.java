package com.orange.barrage.model.feed;

import com.google.protobuf.Message;
import com.mongodb.DBObject;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.common.elasticsearch.ESORMable;
import com.orange.common.utils.MapUtil;
import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.UserProtos;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

/**
 * Created by pipi on 14/12/25.
 */
public class FeedAction extends CommonData implements ProtoBufCoding<BarrageProtos.PBFeedAction>, ESORMable, MapUtil.MakeMapable<ObjectId, FeedAction> {

    public FeedAction(DBObject dbObject) {
        super(dbObject);
    }

    public FeedAction() {
        super();
    }

    public Map<String, Object> getESORM() {
        return null;
    }

    public String getESIndexType() {
        return null;
    }



    @Override
    public List<String> fieldsForIndex() {
        return null;
    }

    public boolean hasFieldForSearch() {
        return false;
    }

    public boolean canBeIndexed() {
        return false;
    }

    public ObjectId getKey() {
        return null;
    }

    public FeedAction getValue() {
        return null;
    }

    public BarrageProtos.PBFeedAction toProtoBufModel() {
        return null;
    }



    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {

    }


    public Class getPBClass(){
        return BarrageProtos.PBFeedAction.class;
    }

}
