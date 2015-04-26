package com.orange.game.model.manager.group;

import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.manager.xiaojinumber.FreePoolManager;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-1-20
 * Time: 下午3:32
 * To change this template use File | Settings | File Templates.
 */
public class GroupUserTimelineManager extends CommonMongoIdListManager<UserAction> {

    public static final String PREFIX = "timeline_group_opus_";

    public static final String GROUP_USER_ALL_OPUS_TIMELINE = "timeline_group_user_opus_all";
    public static final String OPUS_TABLE_NAME = DBConstants.T_OPUS;

    static final ConcurrentHashMap<String, GroupUserTimelineManager> typeMap = new ConcurrentHashMap<String, GroupUserTimelineManager>();
    static final Object typeMapLock = new Object();

    public static final GroupUserTimelineManager groupUserAllOpusTimelineManager =
            new GroupUserTimelineManager(GROUP_USER_ALL_OPUS_TIMELINE, OPUS_TABLE_NAME, UserAction.class);

    public static GroupUserTimelineManager getAllTimelineManager(){
        return groupUserAllOpusTimelineManager;
    }

    public static GroupUserTimelineManager getTimelineManager(String category){

        if (StringUtil.isEmpty(category)){
            return null;
        }

        synchronized (typeMapLock) {

            String mapKey = PREFIX + category+"_";

            if (typeMap.containsKey(mapKey)){
                return typeMap.get(mapKey);
            }
            else{
                GroupUserTimelineManager manager = new GroupUserTimelineManager(mapKey, OPUS_TABLE_NAME, UserAction.class);
                typeMap.putIfAbsent(mapKey, manager);
                return typeMap.get(mapKey);
            }
        }
    }

    public static GroupUserTimelineManager getTimelineManager(int categoryType){
        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(categoryType);
        if (xiaoji == null)
            return null;

        return getTimelineManager(xiaoji.getCategoryName());
    }

    public List<UserAction> getTimelineList(String groupId,int offset,int limit) {
        List<UserAction> userActions = getList( groupId, offset, limit,
                OpusUtils.NORMAL_RETURN_FIELDS,
                DBConstants.F_OPUS_STATUS,
                UserAction.STATUS_DELETE);
        return userActions;
    }

    public void insertOpus(String groupId, String opusId){
        this.insertId(groupId, opusId, false, true);
    }

    public GroupUserTimelineManager(String idListTableName, String idTableName, Class<UserAction> returnDataObjectClass) {
        super(idListTableName, idTableName, returnDataObjectClass);
    }

    @Override
    protected String indexBeforeDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected List<UserAction> invokeOldGetList(String userId, int offset, int limit) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected List<UserAction> invokeOldGetListForConstruct(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
