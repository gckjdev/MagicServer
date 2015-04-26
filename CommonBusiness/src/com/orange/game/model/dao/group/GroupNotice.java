package com.orange.game.model.dao.group;

import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.dao.User;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.model.GroupProtos;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-11
 * Time: 下午4:22
 * To change this template use File | Settings | File Templates.
 */
public class GroupNotice extends CommonData implements ProtoBufCoding<GroupProtos.PBGroupNotice> {


    public static int StatusNormal = 0;
    public static int StatusDelete = 1;


    public static int TypeBulletin = 0;
    public static int TypeRequest = 1;
    public static int TypeExpel = 2;
    public static int TypeAccept = 3;
    public static int TypeReject = 4;
    public static int TypeQuit = 5;
    public static int TypeInviteMember = 6;
    public static int TypeInviteGuest = 7;

    public static int TypeRejectMemberInvitation = 8;
    public static int TypeRejectGuestInvitation = 9;

    public static int TypeAcceptMemberInvitation = 10;
    public static int TypeAcceptGuestInvitation = 11;

    public static int TypeChargeGroup = 12;
    public static int TypeTransferGroupBalance = 13;

    public static int TypeSystemDeductGroupFee = 14;
    public static int TypeSystemDeductGroupFeeFail = 15;


    private User publisher;
    private User target;

    public GroupNotice(DBObject obj) {
        super(obj);
    }

    public GroupNotice(){
        super();
    }


    public void setCreateDate(Date date){
        put(DBConstants.F_CREATE_DATE, date);
    }

    public Date getCreateDate(){
        Date date = getDate(DBConstants.F_CREATE_DATE);
        if (date == null && getObjectId() != null){
            date = new Date(getObjectId().getTimestamp());
        }
        return date;
    }

    private static GroupNotice getNomalNotice(String userId, String groupId, String message, int type) {
        GroupNotice notice = new GroupNotice();
        notice.setNoticeId(new ObjectId());
        notice.setGroupId(groupId);
        notice.setMessage(message);
        notice.setUserId(userId);
        notice.setStatus(GroupNotice.StatusNormal);
        notice.setType(type);
        notice.setCreateDate(new Date());
        return notice;
    }

    private void setNoticeId(ObjectId objectId) {
        put("_id", objectId);
    }

    public static GroupNotice getRequestNotice(String userId, String groupId, String message) {
        GroupNotice notice = getNomalNotice(userId, groupId, message, GroupNotice.TypeRequest);
        return notice;
    }

    public static GroupNotice getExpelNotice(String userId, String groupId, String targetId, String message) {
        GroupNotice notice = getNomalNotice(userId, groupId, message, GroupNotice.TypeExpel);
        notice.setTargetUid(targetId);
        return notice;

    }

    public static GroupNotice getBulletinNotice(String userId, String groupId, String message) {
        GroupNotice notice = getNomalNotice(userId, groupId, message, GroupNotice.TypeBulletin);
        return notice;
    }

    public static GroupNotice getAcceptNotice(String userId, String groupId, String targetId) {
        GroupNotice notice = getNomalNotice(userId, groupId, null, GroupNotice.TypeAccept);
        notice.setTargetUid(targetId);
        return notice;
    }

    public static GroupNotice getRejectNotice(String userId, String groupId, String targetId, String message) {
        GroupNotice notice = getNomalNotice(userId, groupId, message, GroupNotice.TypeReject);
        notice.setTargetUid(targetId);
        return notice;
    }

    public static GroupNotice getQuitNotice(String userId, String groupId) {
        GroupNotice notice = getNomalNotice(userId, groupId, null, GroupNotice.TypeQuit);
        notice.setTargetUid(userId);
        return notice;

    }

    @Override
    public GroupProtos.PBGroupNotice toProtoBufModel() {
        GroupProtos.PBGroupNotice.Builder builder = GroupProtos.PBGroupNotice.newBuilder();
        builder.setType(getType());
        builder.setStatus(getStatus());
        builder.setNoticeId(getNoticeId());
        Date date = getCreateDate();
        if (date != null){
            builder.setCreateDate((int)(date.getTime()/1000));
        }
        if (getMessage() != null) {
            builder.setMessage(getMessage());
        }

        if (getGroupId() != null) {
            builder.setGroupId(getGroupId());
        }

        if (getGroupName() != null){
            builder.setGroupName(getGroupName());
        }
        if (publisher != null) {
            builder.setPublisher(publisher.toPBUser());
        }

        if (target != null){
            builder.setTarget(target.toPBUser());
        }
        builder.setAmount(getAmount());

        return builder.build();
    }


    @Override
    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {
        builder.addNoticeList(toProtoBufModel());
    }


    public User getPublisher() {
        return publisher;
    }

    public void setPublisher(User publisher) {
        this.publisher = publisher;
    }

    public User getTarget() {
        return target;
    }

    public void setTarget(User target) {
        this.target = target;
    }

    public String getNoticeId() {
        return getObjectId().toString();
    }

    public void setNoticeId(String noticeId) {
        put("_id", new ObjectId(noticeId));
    }

    public String getMessage() {
        return getString(DBConstants.F_MESSAGE_TEXT);
    }

    public void setMessage(String message) {
        put(DBConstants.F_MESSAGE_TEXT, message);
    }

    public String getGroupId() {
        ObjectId oid = getObjectId(DBConstants.F_GROUPID);
        if (oid != null) {
            return oid.toString();
        }
        return null;
    }

    public void setGroupId(String groupId) {
        put(DBConstants.F_GROUPID, new ObjectId(groupId));
    }

    public int getType() {
        return getInt(DBConstants.F_TYPE);
    }

    public void setType(int type) {
        put(DBConstants.F_TYPE, type);
    }

    public int getStatus() {
        return getInt(DBConstants.F_STATUS);
    }

    public void setStatus(int status) {
        put(DBConstants.F_STATUS, status);
    }

    public String getUserId() {
        ObjectId oid = getObjectId(DBConstants.F_UID);
        if (oid != null) {
            return oid.toString();
        }
        return null;
    }

    public void setUserId(String userID) {
        put(DBConstants.F_UID, new ObjectId(userID));
    }

    public String getTargetUid() {
        ObjectId oid = getObjectId(DBConstants.F_TARGET_UID);
        if (oid != null) {
            return oid.toString();
        }
        return null;
    }

    public void setTargetUid(String targetUId) {
        put(DBConstants.F_TARGET_UID, new ObjectId(targetUId));
    }


    public void setGroupName(String name) {
        put(DBConstants.F_GROUPNAME, name);
    }

    public String getGroupName() {
        return getString(DBConstants.F_GROUPNAME);
    }


    public int getTitleId(){
        return getInt(DBConstants.F_TITLE_ID);
    }

    public void setTitleId(int titleId){
        put(DBConstants.F_TITLE_ID, titleId);
    }

    private void setAmount(int amount) {
        put(DBConstants.F_AMOUNT, amount);
    }

    private int getAmount() {
        return getInt(DBConstants.F_AMOUNT);
    }



    public static GroupNotice getInviteMemberNotice(String userId, String groupId, String targetUid, int titleId) {
        GroupNotice notice = getNomalNotice(userId, groupId, null, TypeInviteMember);
        notice.setTitleId(titleId);
        notice.setTargetUid(targetUid);
        return notice;
    }


    public static GroupNotice getInviteGuestNotice(String userId, String groupId, String targetUid) {
        GroupNotice notice = getNomalNotice(userId, groupId, null, TypeInviteGuest);
        notice.setTargetUid(targetUid);
        return notice;
    }


    public static GroupNotice getRejectMemberInvitationNotice(String userId, String groupId) {
        GroupNotice notice = getNomalNotice(userId, groupId, null, TypeRejectMemberInvitation);
        return notice;
    }

    public static GroupNotice getRejectGuestInvitationNotice(String userId, String groupId) {
        GroupNotice notice = getNomalNotice(userId, groupId, null, TypeRejectGuestInvitation);
        return notice;
    }

    public static GroupNotice getAcceptMemberInvitationNotice(String userId, String groupId) {
        GroupNotice notice = getNomalNotice(userId, groupId, null, TypeAcceptMemberInvitation);
        return notice;
    }

    public static GroupNotice getAcceptGuestInvitationNotice(String userId, String groupId) {
        GroupNotice notice = getNomalNotice(userId, groupId, null, TypeAcceptGuestInvitation);
        return notice;
    }

    public boolean isInvitation() {
        return getType() == TypeInviteMember || getType() == TypeInviteGuest;
    }

    public boolean isRequest() {
        return getType() == TypeRequest;
    }

    public static GroupNotice getChargeGroupNotice(String userId, String groupId, int amount) {
        GroupNotice notice = getNomalNotice(userId, groupId, null, TypeChargeGroup);
        notice.setAmount(amount);
        return notice;
    }

    public static GroupNotice getTransferBalanceNotice(String userId, String groupId, String targetUid, int amount) {
        GroupNotice notice = getNomalNotice(userId, groupId, null, TypeTransferGroupBalance);
        notice.setTargetUid(targetUid);
        notice.setAmount(-amount);
        return notice;

    }

    public static GroupNotice getSystemDeductGroupFeeNotice(String groupId, int amount) {
        GroupNotice notice = getNomalNotice(DBConstants.SYSTEM_USERID, groupId, null, TypeSystemDeductGroupFee);
        notice.setAmount(amount);
        return notice;
    }

    public static GroupNotice getSystemDeductGroupFeeFailNotice(String groupId, int amount) {
        GroupNotice notice = getNomalNotice(DBConstants.SYSTEM_USERID, groupId, null, TypeSystemDeductGroupFeeFail);
        notice.setAmount(amount);
        return notice;
    }

}
