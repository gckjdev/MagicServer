package com.orange.barrage.model.feed;

import com.orange.barrage.constant.BarrageConstants;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonCassandraIdListManager;
import com.orange.game.model.dao.Message;

import java.util.List;

/**
 * Created by pipi on 14/12/8.
 */
public class UserTimelineFeedManager extends CommonCassandraIdListManager<Feed> {

    private static UserTimelineFeedManager ourInstance = new UserTimelineFeedManager();

    public static UserTimelineFeedManager getInstance() {
        return ourInstance;
    }

    private UserTimelineFeedManager() {
        super(BarrageConstants.T_USER_FEED_TIMELINE, BarrageConstants.T_FEED, Feed.class);
    }

    public void insertUserFeed(String userId, String feedId, String optionData){
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(feedId)){
            log.info("<insertUserFeed> but userId or feedId is EMPTY/NULL");
            return;
        }

        String key = userId;
        insertIndex(key, feedId, optionData);
    }

    public void deleteUserFeed(String userId, String feedId){
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(feedId)){
            log.info("<deleteUserFeed> but userId or feedId is EMPTY/NULL");
            return;
        }

        String key = userId;
        log.info("<deleteUserFeed> key="+key+", feedId="+feedId);
        deleteIndex(key, feedId);
    }

    public List<Feed> getUserTimeline(String userId, String offsetFeedId, int limit, boolean forward){
        String key = userId;

        String startOffsetId = null;
        String endOffsetId = null;

        if (forward)
            endOffsetId = offsetFeedId;
        else
            startOffsetId = offsetFeedId;

        return getList(key, startOffsetId, endOffsetId, limit, null, 0, null);
    }

}
