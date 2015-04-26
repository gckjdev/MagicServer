package com.orange.barrage.model.feed;

import com.google.protobuf.Message;
import com.mongodb.DBObject;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.common.elasticsearch.ESORMable;
import com.orange.common.utils.MapUtil;
import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.model.DrawProtos;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.UserProtos;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pipi on 14/12/8.
 */
public class Feed extends CommonData  implements ProtoBufCoding<BarrageProtos.PBFeed>, ESORMable, MapUtil.MakeMapable<ObjectId, Feed> {

    public Feed(DBObject dbObject) {
        super(dbObject);
    }

    public Feed() {
        super();
    }

    public Map<String, Object> getESORM() {
        return null;
    }

    public String getESIndexType() {
        return BarrageConstants.ES_INDEX_TYPE_FEED;
    }

    @Override
    public List<String> fieldsForIndex() {
        List<String> list = new ArrayList<String>();

        list.add(BarrageConstants.F_FEED_ID);

        // TODO more fields

        return list;
    }


    public boolean hasFieldForSearch() {
        if (dbObject == null)
            return false;

        List<String> fields = fieldsForIndex();
        for (String field : fields){
            if (dbObject.containsField(field)){
                return true;
            }
        }

        return false;
    }

    public boolean canBeIndexed() {
        return true;
    }

    public ObjectId getKey() {
        return getObjectId();
    }

    public Feed getValue() {
        return this;
    }

    public BarrageProtos.PBFeed toProtoBufModel() {
        return toPB(BarrageProtos.PBFeed.newBuilder(), null);
    }

    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {

    }

    public String getPbKeyFieldName(){
        return BarrageConstants.F_FEED_ID;
    }

    public Class getPBClass(){
        return BarrageProtos.PBFeed.class;
    }

}
