package com.orange.barrage.model.chat;

import com.mongodb.DBObject;
import com.orange.barrage.model.user.User;
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
 * Created by pipi on 15/4/18.
 */
public class Agent extends User implements ProtoBufCoding<UserProtos.PBUser>, ESORMable, MapUtil.MakeMapable<ObjectId, User> {

    public Agent(DBObject dbObject) {
        super(dbObject);
    }

    public Agent() {
        super();
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
    public Agent getValue() {
        return null;
    }

    @Override
    public UserProtos.PBUser toProtoBufModel() {
        UserProtos.PBUser.Builder builder = UserProtos.PBUser.newBuilder();
        return toPB(builder, null);
    }

    @Override
    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {

    }

    public String getAgentId() {
        return getUserId();
    }
}
