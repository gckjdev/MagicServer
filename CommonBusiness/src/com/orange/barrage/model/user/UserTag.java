package com.orange.barrage.model.user;

import com.mongodb.DBObject;
import com.orange.common.elasticsearch.ESORMable;
import com.orange.common.utils.MapUtil;
import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.protocol.message.UserProtos;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

/**
 * Created by pipi on 15/1/20.
 */
public class UserTag  extends CommonData implements ProtoBufCoding<UserProtos.PBUserTag>, ESORMable, MapUtil.MakeMapable<ObjectId, UserTag> {

    public UserTag(DBObject object) {
        super(object);
    }

    public Map<String, Object> getESORM() {
        return null;
    }

    public String getESIndexType() {
        return null;
    }

    public String getESIndexName() {
        return null;
    }

    public String getID() {
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

    public UserTag getValue() {
        return null;
    }

    public UserProtos.PBUserTag toProtoBufModel() {
        UserProtos.PBUserTag.Builder builder = UserProtos.PBUserTag.newBuilder();
        return toPB(builder, null);
    }

    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {
    }
}
