package com.orange.barrage.model.chat;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.feed.Feed;
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
 * Created by pipi on 15/4/18.
 */
public class Chat extends CommonData implements ProtoBufCoding<UserProtos.PBChat>, ESORMable, MapUtil.MakeMapable<ObjectId, Chat> {

    public Chat(DBObject dbObject) {
        super(dbObject);
    }

    public Chat() {
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
    public Chat getValue() {
        return null;
    }

    @Override
    public UserProtos.PBChat toProtoBufModel() {
        UserProtos.PBChat.Builder builder = UserProtos.PBChat.newBuilder();
        return toPB(builder, null);
    }

    public Class getPBClass(){
        return UserProtos.PBChat.class;
    }

    @Override
    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {

    }

    public String getChatId() {
        return getString(BarrageConstants.F_CHAT_ID);
    }

    public String getToUserId() {
        return getString(BarrageConstants.F_TO_USER_ID);
    }

    public void setToUserId(String agentId) {
        put(BarrageConstants.F_TO_USER_ID, agentId);

    }

    public void setToUser(Agent agent) {

        BasicDBObject obj = new BasicDBObject();
        obj.putAll(agent.getDbObject());
        obj.remove("_id");

        put(BarrageConstants.F_TO_USER, obj);
    }

    public Object getToUser() {
        return dbObject.get(BarrageConstants.F_TO_USER);
    }
}
