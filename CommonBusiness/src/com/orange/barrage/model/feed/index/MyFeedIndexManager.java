package com.orange.barrage.model.feed.index;

import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.feed.Feed;
import com.orange.game.model.common.CommonMongoIdListManager;

import java.util.List;

/**
 * Created by pipi on 15/3/20.
 */
public class MyFeedIndexManager extends CommonMongoIdListManager<Feed> {

    private static MyFeedIndexManager ourInstance = new MyFeedIndexManager();

    public static MyFeedIndexManager getInstance() {
        return ourInstance;
    }

    private MyFeedIndexManager() {
        super(BarrageConstants.T_MY_FEED,  BarrageConstants.T_FEED, Feed.class);
    }

    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List<Feed> invokeOldGetList(String userId, int offset, int limit) {
        return null;
    }

    @Override
    protected List<Feed> invokeOldGetListForConstruct(String key) {
        return null;
    }

    public void insertFeed(String userId, String feedId){
        insertId(userId, feedId, false, false);
    }

    public void removeFeed(String userId, String feedId){
        removeId(userId, feedId, false);
    }

    public List<Feed> getUserFeedList(String userId, int offset, int limit) {
        return getList(userId, offset, limit, null, null, 0);
    }
}
