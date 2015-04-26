package com.orange.game.model.manager.group.index;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.group.GroupNotice;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-23
 * Time: 上午10:27
 * To change this template use File | Settings | File Templates.
 */
public class UserNoticeIndexManager extends CommonMongoIdListManager<GroupNotice> {

    private static UserNoticeIndexManager requestManager = new UserNoticeIndexManager("user_request", DBConstants.T_GROUP_NOTICE);
    private static UserNoticeIndexManager noticeManager = new UserNoticeIndexManager("user_notice", DBConstants.T_GROUP_NOTICE);


    private UserNoticeIndexManager(String idListTableName, String idTableName) {
        super(idListTableName, idTableName, GroupNotice.class);
        this.autoFixTotalSize = true;
    }

    public static UserNoticeIndexManager getRequestManager() {
        return requestManager;
    }

    public static UserNoticeIndexManager getNoticeManager() {
        return noticeManager;
    }


    public void insertIndex(String userId, String noticeId, String noticePublisher){

        if (userId == null){
            return;
        }

        if (userId.equalsIgnoreCase(noticePublisher)){
            // the notice is to self user, don't increase new notice count
            insertId(userId, noticeId, false, true);
        }
        else{
            insertId(userId, noticeId, true, true);
        }
    }

    public void insertIndexWithOids(String noticeId, Collection<ObjectId> userIdList, String noticePublisher){
        log.info("<UserNoticeIndexManager> insertIndexWithOids, uidList count = " + userIdList.size() + ", publisher="+noticePublisher);
        for (ObjectId oid : userIdList){
            insertIndex(oid.toString(), noticeId, noticePublisher);
        }
    }



//    public void insertIndexWithSIds( String noticeId, List<String> userIdList){
//        for (String userId : userIdList){
//            insertIndex(userId, noticeId);
//        }
//    }

    public List<GroupNotice> getList(String userId, int offset, int limit)
    {
        return getList(userId, offset, limit, null, DBConstants.F_STATUS, 1);
    }

    public void removeId(String userId, String noticeId)
    {
        removeId(userId, noticeId, true);
    }

    @Override
    protected String indexBeforeDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected List<GroupNotice> invokeOldGetList(String userId, int offset, int limit) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected List<GroupNotice> invokeOldGetListForConstruct(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
