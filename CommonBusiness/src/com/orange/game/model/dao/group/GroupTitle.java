package com.orange.game.model.dao.group;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.model.GroupProtos;
import org.bson.types.ObjectId;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-2
 * Time: 下午2:05
 * To change this template use File | Settings | File Templates.
 */
public class GroupTitle extends CommonData implements ProtoBufCoding<GroupProtos.PBGroupTitle> {


    public static final int MEMBER = GroupRole.MEMBER.getRole();
    public static final int ADMIN = GroupRole.ADMIN.getRole();
    public static final int GUEST = GroupRole.GUEST.getRole();
    public static final int NONE = GroupRole.NONE.getRole();
    public static final int CUSTOM_START = 11;

    public GroupTitle() {
        super();
    }

    public GroupTitle(DBObject dbObject) {
        super(dbObject);
    }

    public int getTitleId() {
        return getInt(DBConstants.F_TITLE_ID);
    }


    public void setTitleId(int titleId) {
        put(DBConstants.F_TITLE_ID, titleId);
    }

    public String getTitle() {
        return getString(DBConstants.F_TITLE);
    }

    public void setTitle(String title) {
        put(DBConstants.F_TITLE, title);
    }

       /*
    public int getPermission() {
        return getInt(DBConstants.F_PERMISSION);
    }

    public void setPermission(int permission) {
        put(DBConstants.F_PERMISSION, permission);
    }
         */


    public String getGroupId(){
        ObjectId groupID = getObjectId(DBConstants.F_GROUPID);
        if (groupID != null){
            return groupID.toString();
        }
        return null;
    }


    public void setGroupId(String groupId){
        if (groupId != null){
            put(DBConstants.F_GROUPID, new ObjectId(groupId));
        }
    }

    public List<ObjectId> getUserIdList(){
        List list = getList(DBConstants.F_USERID_LIST);
        if (list != null){
            return (List<ObjectId>)list;
        }
        return Collections.emptyList();
    }


    @Override
    public GroupProtos.PBGroupTitle toProtoBufModel() {
        GroupProtos.PBGroupTitle.Builder builder = GroupProtos.PBGroupTitle.newBuilder();
//        builder.setPermission(getPermission());
        builder.setTitleId(getTitleId());
        builder.setTitle(StringUtil.getEmptyStringWhenNull(getTitle()));
        return builder.build();
    }

    @Override
    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {


    }

    public void addUid(ObjectId objectId) {
        List list = getUserIdList();
        if (list.isEmpty()){
            list = new BasicDBList();
            put(DBConstants.F_USERID_LIST, list);
        }
        list.add(objectId);
    }
}
