package com.orange.game.model.dao.group;

import com.google.protobuf.GeneratedMessage;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.elasticsearch.ESORMable;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.MapUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.utils.ImageUploadManager;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GroupProtos;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-2
 * Time: 上午10:58
 * To change this template use File | Settings | File Templates.
 */
public class Group extends CommonData implements ProtoBufCoding<GroupProtos.PBGroup>, ESORMable, MapUtil.MakeMapable<ObjectId, Group> {
    public static final int StatusDelete = 1;
    public static final int StatusNormal = 0;
    public static final int StatusClose = 2;
    public static final int CONST_QUIT_GROUP_FEE = 188;
    //    String groupId, name, signature, desc, bgImage, medalImage;
//    int level, fame, balance, createDate, memberFee;
//    User creator;
//    List<GroupTitle> titles;
//    List<User> admins;
//    List<GroupTitle> users;
//    List<GroupTitle> guests;
    User creator;
    List<User> admins;
    List<User> guests;
    List<GroupUsersByTitle> users;

    public Group() {
        super();
    }

    public Group(DBObject dbObject) {
        super(dbObject);
    }

    public static <T extends ProtoBufCoding> List<? extends GeneratedMessage> getPBlist(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        List retList = new ArrayList();
        for (T obj : list) {
            GeneratedMessage pbObj = obj.toProtoBufModel();
            if (pbObj != null) {
                retList.add(pbObj);
            }
        }
        return retList;
    }

    public static DBObject getSimpleReturnFields() {
        DBObject fields = new BasicDBObject();
        fields.put(DBConstants.F_ADMINUID_LIST, 0);
        fields.put(DBConstants.F_GUESTUID_LIST, 0);
        fields.put(DBConstants.F_LAST_POST, 0);
        return fields;
    }

    public static DBObject getUpdateByLevel(int level) {
        Group group = new Group();
        group.setLevel(level);
        return group.getDbObject();
    }

    private static int getGuestCapacityByLevel(int level) {
        //TODO design the method.
        return level * 5;
    }

    private static int getCapacityByLevel(int level) {
        //TODO design the method.
        return level * 10;
    }

    private static int getCustomTitleCapacityByLevel(int level) {
        //TODO design the method.
        return level * 1;
    }

    public static List<String> getSearchCandidateFields() {
        List<String> fileds = new ArrayList<String>();
        fileds.add(DBConstants.ES_NAME);
        fileds.add(DBConstants.ES_DESCRIPTION);
        fileds.add(DBConstants.ES_SIGNATURE);
        return fileds;
    }

    public static int upgradeFee(int oldLevel, int newLevel) {
        return Math.max(0, (newLevel - oldLevel) * 100);
    }

    @Override
    public GroupProtos.PBGroup toProtoBufModel() {
        GroupProtos.PBGroup.Builder builder = GroupProtos.PBGroup.newBuilder();
        builder.setGroupId(getGroupId());
        builder.setName(getName());
        builder.setLevel(getLevel());
        builder.setFame(getFame());
        builder.setBalance(getBalance());
        builder.setMemberFee(getMemberFee());
        builder.setStatus(getStatus());
        builder.setFanCount(getFanCount());
        builder.setSize(getSize());
        builder.setCapacity(getCapacity());
        builder.setGuestSize(getGuestSize());
        builder.setGuestCapacity(getGuestCapacity());
        builder.setTitleCapacity(getTitleCapacity());

        if (getStatusDesc() != null) {
            builder.setStatusDesc(getStatusDesc());
        }

        if (getCreateDate() != null) {
            builder.setCreateDate((int) (getCreateDate().getTime() / 1000));
        }

        //set strings
        if (getSignature() != null) {
            builder.setSignature(getSignature());
        }
        if (getDesc() != null) {
            builder.setDesc(getDesc());
        }
        if (getBgImage() != null) {
            builder.setBgImage(getBgImage());
        }

        if (getMedalImage() != null) {
            builder.setMedalImage(getMedalImage());
        }

        //set users && titles

        List<GroupProtos.PBGroupTitle> pbTitles = (List<GroupProtos.PBGroupTitle>) getPBlist(getTitles());
        if (pbTitles != null) {
            builder.addAllTitles(pbTitles);
        }

        if (getCreator() != null) {
            builder.setCreator(getCreator().toProtoBufModel());
        }

        List<GameBasicProtos.PBGameUser> pbAdmins = (List<GameBasicProtos.PBGameUser>) getPBlist(getAdmins());
        if (pbAdmins != null) {
            builder.addAllAdmins(pbAdmins);
        }

        List<GameBasicProtos.PBGameUser> pbGuests = (List<GameBasicProtos.PBGameUser>) getPBlist(getGuests());
        if (pbGuests != null) {
            builder.addAllGuests(pbGuests);
        }
        return builder.build();
    }

    @Override
    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {
        builder.addGroupList(toProtoBufModel());
    }

    public String getGroupId() {
        return getStringObjectId();
    }

    public void setGroupId(String groupId) {
        put("_id", new ObjectId(groupId));
    }

    public String getName() {
        return getString(DBConstants.F_NAME);
    }

    public void setName(String name) {
        put(DBConstants.F_NAME, name);
    }

    public String getSignature() {
        return getString(DBConstants.F_SIGNATURE);
    }

    public void setSignature(String signature) {
        put(DBConstants.F_SIGNATURE, signature);
    }

    public String getDesc() {
        return getString(DBConstants.F_DESC);
    }

    public void setDesc(String desc) {
        put(DBConstants.F_DESC, desc);
    }

    public String getBgImage() {
        String imageUrl = getString(DBConstants.F_BACKGROUND);
        if (imageUrl == null) {
            return null;
        }
        return ImageUploadManager.getGroupImageManager().getRemoteURL(imageUrl);
    }

    public String getMedalImage() {

        String imageUrl = getString(DBConstants.F_IMAGE);
        if (imageUrl == null) {
            return null;
        }
        return ImageUploadManager.getGroupImageManager().getRemoteURL(imageUrl);
    }

    public void setMedalImage(String localUrl) {
        put(DBConstants.F_IMAGE, localUrl);
    }

    public void setBGImage(String localUrl) {
        put(DBConstants.F_BACKGROUND, localUrl);
    }

    public int getLevel() {
        return getInt(DBConstants.F_LEVEL);
    }

    public void setLevel(int level) {
        put(DBConstants.F_LEVEL, level);
    }

    public int getFame() {
        return getInt(DBConstants.F_FAME);
    }

    public int getBalance() {
        return getInt(DBConstants.F_GROUP_BALANCE);
    }

    public Date getCreateDate() {
        return getDate(DBConstants.F_CREATE_DATE);
    }

    public void setCreateDate(Date date) {
        put(DBConstants.F_CREATE_DATE, date);
    }

    public int getMemberFee() {
        return getInt(DBConstants.F_MEMBER_FEE);
    }

    public void setMemberFee(int fee) {
        put(DBConstants.F_MEMBER_FEE, fee);
    }

    public List<GroupTitle> getTitles() {
        BasicDBList list = getList(DBConstants.F_GROUP_TITLES);
        if (list == null) {
            return Collections.emptyList();
        }
        int size = list.size();
        List<GroupTitle> groupTitles = new ArrayList<GroupTitle>(size);
        for (int i = 0; i < size; i++) {
            DBObject obj = (DBObject) list.get(i);
            groupTitles.add(new GroupTitle(obj));
        }
        return groupTitles;
    }

    public ObjectId getCreatorUid() {
        return (ObjectId) getObject(DBConstants.F_CREATE_USERID);
    }

    public void setCreatorUid(String userId) {
        put(DBConstants.F_CREATE_USERID, new ObjectId(userId));
    }

    public List<ObjectId> getAdminUids() {
        return getList(DBConstants.F_ADMINUID_LIST, ObjectId.class);
    }

    public List<ObjectId> getGuestUids() {
        return getList(DBConstants.F_GUESTUID_LIST, ObjectId.class);
    }

    //need to set
    public void loadAdminsAndGuests(MongoDBClient mongoDBClient) {
        GroupManager.loadAdminsAndGuests(mongoDBClient, this);
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        log.info("<setCreator> creator =" + creator);
        this.creator = creator;
    }

    public List<User> getAdmins() {
        return admins;
    }

    public void setAdmins(List<User> admins) {
        this.admins = admins;
    }

    public List<User> getGuests() {
        return guests;
    }

    public void setGuests(List<User> guests) {
        this.guests = guests;
    }

    public List<GroupUsersByTitle> getUsers() {
        return users;
    }

    public void setUsers(List<GroupUsersByTitle> users) {
        this.users = users;
    }

    public String getGameId() {
        return getString(DBConstants.F_GAME_ID);
    }

    public void setGameId(String appId) {
        put(DBConstants.F_GAME_ID, appId);
    }

    public boolean isFull() {
        return getSize() >= getCapacity();
    }

    public int getCapacity() {
        return getCapacityByLevel(getLevel());
    }

    public int getSize() {
        return getInt(DBConstants.F_SIZE);
    }

    public void setSize(int size) {
        put(DBConstants.F_SIZE, size);
    }

    public int getGuestCapacity() {
        return getGuestCapacityByLevel(getLevel());
    }

    public int getGuestSize() {
        return getInt(DBConstants.F_GUEST_SIZE);
    }

    public void setGuestSize(int size) {
        put(DBConstants.F_GUEST_SIZE, size);
    }

    public int getTitleCapacity() {
        return getCustomTitleCapacityByLevel(getLevel());
    }

    public boolean isGuestFull() {
        return getGuestSize() >= getGuestCapacity();
    }

    public void addAdminId(String userId) {
        BasicDBList aids = getList(DBConstants.F_ADMINUID_LIST);
        if (aids == null || aids.isEmpty()) {
            aids = new BasicDBList();
        }
        aids.add(new ObjectId(userId));
        put(DBConstants.F_ADMINUID_LIST, aids);
    }

    public int getStatus() {
        return getInt(DBConstants.F_STATUS);
    }

    public void setStatus(int status) {
        put(DBConstants.F_STATUS, status);
    }

    public int getFanCount() {
        return getInt(DBConstants.F_FAN_COUNT);
    }

    public void setFanCount(int count) {
        put(DBConstants.F_FAN_COUNT, count);
    }

    public String getStatusDesc() {
        return getString(DBConstants.F_STATUS_DESC);
    }

    public void setStatusDesc(String desc) {
        put(DBConstants.F_STATUS_DESC, desc);
    }

    @Override
    public Map<String, Object> getESORM() {
        if (getDbObject() == null || getGroupId() == null) {
            return null;
        }
        Map<String, Object> ormMap = new HashMap<String, Object>(2);
        String name = StringUtil.getEmptyStringWhenNull(getName());
        String description = StringUtil.getEmptyStringWhenNull(getDesc());
        String signature = StringUtil.getEmptyStringWhenNull(getSignature());
        ormMap.put(DBConstants.ES_NAME, name);
        ormMap.put(DBConstants.ES_DESCRIPTION, description);
        ormMap.put(DBConstants.ES_SIGNATURE, signature);
        ormMap.put(DBConstants.ES_GROUP_ID, getID());
        return ormMap;
    }

    @Override
    public String getESIndexType() {
        return DBConstants.ES_INDEX_TYPE_GROUP;
    }

    @Override
    public String getID() {
        return getStringObjectId();
    }

    @Override
    public List<String> fieldsForIndex() {
        return null;
    }

    @Override
    public String getESIndexName() {
        return DBConstants.ES_INDEX_NAME;
    }

    @Override
    public boolean hasFieldForSearch() {
        if (getDbObject() == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canBeIndexed() {
        return true;
    }

    public boolean isUserInGuestList(String userId) {

        if (StringUtil.isEmpty(userId)) {
            return true;
        }

        List<ObjectId> guestIdList = getGuestUids();
        if (guestIdList == null || guestIdList.size() == 0)
            return false;

        for (ObjectId id : guestIdList) {
            if (userId.equalsIgnoreCase(id.toString())) {
                return true;
            }
        }

        return false;
    }

    public boolean isUserInAdminList(String userId) {

        if (StringUtil.isEmpty(userId)) {
            return false;
        }

        List<ObjectId> list = getAdminUids();
        if (list == null || list.size() == 0)
            return false;

        for (ObjectId id : list) {
            if (userId.equalsIgnoreCase(id.toString())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ObjectId getKey() {
        return getObjectId();
    }

    @Override
    public Group getValue() {
        return this;
    }

    public int getMonthlyFee() {
        return MonthlyFeeForLevel(getLevel());
    }

    private int MonthlyFeeForLevel(int level) {
        return level * 100;
    }

    public List<String> getOffUsers() {
        return getStringList(DBConstants.F_OFF_USERS);
    }

    public boolean hasChargeCurrentMonth(Date date) {

        String lastChargeMonth = getString(DBConstants.F_LAST_CHARGE_MONTH);

        if (lastChargeMonth == null || date == null){
            // not data is recorded, not charge yet
            return false;
        }

        String current = DateUtil.dateToChineseStringByFormat(date, "yyyyMM");
        if (current.compareTo(lastChargeMonth) > 0){
            // current date is greater than record data, not charge yet
            return false;
        }

        return true;
    }

    public String getLastChargeMonth() {
        return getString(DBConstants.F_LAST_CHARGE_MONTH);
    }
}
