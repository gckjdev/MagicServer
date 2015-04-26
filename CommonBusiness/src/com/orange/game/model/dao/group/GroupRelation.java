package com.orange.game.model.dao.group;

import com.mongodb.DBObject;
import com.orange.common.utils.MapUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.model.GroupProtos;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-6
 * Time: 下午4:35
 * To change this template use File | Settings | File Templates.
 */
public class GroupRelation extends CommonData implements ProtoBufCoding<GroupProtos.PBGroupUserRole>, MapUtil.MakeMapable<ObjectId, GroupRelation> {


    private String groupName;

    public GroupRelation() {
        super();
    }

    public GroupRelation(DBObject dbObject) {
        super(dbObject);
    }

    public ObjectId getUserIdObjectId()
    {
        return (ObjectId) getObject(DBConstants.F_UID);
    }

    public String getUserId() {
        return getObject(DBConstants.F_UID).toString();
    }

    public void setUserId(String userId) {
        put(DBConstants.F_UID, new ObjectId(userId));
    }

    public String getGroupId() {
        return getObject(DBConstants.F_GROUPID).toString();
    }

    public void setGroupId(String groupId) {
        put(DBConstants.F_GROUPID, new ObjectId(groupId));
    }

    /*
    public void setTitleId(int titleId){
        put(DBConstants.F_TITLE_ID, titleId);
    }

    public int getTitleId(){
        if (getObject(DBConstants.F_TITLE_ID) == null){
            return GroupTitle.MEMBER;
        }
        return getInt(DBConstants.F_TITLE_ID);
    }
      */

    public void setRole(GroupRole role) {
        put(DBConstants.F_ROLE, role.getRole());
    }

    public GroupRole getRole()
    {
        int role = getInt(DBConstants.F_ROLE);
        GroupRole groupRole = GroupRole.valueOf(role);
        return groupRole;
    }


    public boolean isMember(){
        return getRole().isMember();
    }
    

    public int getPermission() {
        return getRole().getPermission();
    }

    public static DBObject queryBy(String userId, String groupId) {
        GroupRelation query = new GroupRelation();
        query.setUserId(userId);
        query.setGroupId(groupId);
        return query.getDbObject();
    }

    public Date getJoinedDate() {
        return getDate(DBConstants.F_JOINED_DATE);
    }

    public void setJoinedDate(Date joinedDate) {
        put(DBConstants.F_JOINED_DATE, joinedDate);
    }


    public void setRequestMessage(String message) {
        put(DBConstants.F_DESC, message);
    }

    public String getRequestMessage() {
        return getString(DBConstants.F_DESC);
    }


    public static GroupRelation getDefaultRelation()
    {
        GroupRelation relation = new GroupRelation();
        relation.setRole(GroupRole.NONE);
        return relation;
    }


    public GroupProtos.PBGroupUserRole toGroupUserRole(){
        GroupProtos.PBGroupUserRole.Builder builder = GroupProtos.PBGroupUserRole.newBuilder();
        builder.setGroupId(getGroupId());
        builder.setRole(getRole().getRole());
        builder.setPermission(getRole().getPermission());
        if (groupName != null){
             builder.setGroupName(groupName);
        }
        return builder.build();
    }

    @Override
    public GroupProtos.PBGroupUserRole toProtoBufModel() {
        return toGroupUserRole();
    }

    @Override
    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {
        builder.addGroupRole(toGroupUserRole());
    }




    @Override
    public ObjectId getKey() {
        return (ObjectId) getObject(DBConstants.F_UID);
    }

    @Override
    public GroupRelation getValue() {
        return this;
    }

    public void setGroupName(String name) {
        this.groupName = name;
    }

    public String getGroupName(){
        return this.groupName;
    }
}
