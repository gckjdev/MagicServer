package com.orange.barrage.model.feed.index;

import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.feed.Feed;
import com.orange.barrage.model.user.User;
import com.orange.game.model.common.CommonMongoIdComplexListManager;
import com.orange.game.model.common.CommonMongoIdListManager;

import java.util.List;

/**
 * Created by pipi on 15/3/20.
 */
public class MyRelatedFeedIndexManager  extends CommonMongoIdListManager<Feed> {

    private static MyRelatedFeedIndexManager ourInstance = new MyRelatedFeedIndexManager();

    public static MyRelatedFeedIndexManager getInstance() {
        return ourInstance;
    }

    private MyRelatedFeedIndexManager() {
        super(BarrageConstants.T_MY_RELATED_FEED, BarrageConstants.T_FEED, Feed.class);
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
        insertId(userId, feedId, NOT_ALLOW_DUPLICATE, false, false, true);
    }

    public void removeFeed(String userId, String feedId){
        removeId(userId, feedId, false);
    }

}
