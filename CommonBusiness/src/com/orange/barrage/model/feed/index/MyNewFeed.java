package com.orange.barrage.model.feed.index;

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
 * Created by pipi on 15/3/20.
 */
public class MyNewFeed extends CommonData implements ProtoBufCoding<BarrageProtos.PBMyNewFeed>, ESORMable, MapUtil.MakeMapable<ObjectId, MyNewFeed> {

    public MyNewFeed(DBObject newFeedObj) {
        super(newFeedObj);
    }

    @Override
    public Map<String, Object> getESORM() {
        return null;
    }

    @Override
    public String getESIndexType() {
        return null;
    }

    @Override
    public List<String> fieldsForIndex() {
        return null;
    }

    @Override
    public boolean hasFieldForSearch() {
        return false;
    }

    @Override
    public boolean canBeIndexed() {
        return false;
    }

    @Override
    public ObjectId getKey() {
        return null;
    }

    @Override
    public MyNewFeed getValue() {
        return null;
    }

    @Override
    public BarrageProtos.PBMyNewFeed toProtoBufModel() {
        BarrageProtos.PBMyNewFeed.Builder builder = BarrageProtos.PBMyNewFeed.newBuilder();
        return toPB(builder, null);
    }

    @Override
    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {

    }

    public int getCount() {
        return getInt(BarrageConstants.F_COUNT);
    }

    public Class getPBClass(){
        return BarrageProtos.PBMyNewFeed.class;
    }
}
