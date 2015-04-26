package com.orange.barrage.model.feed.index;

import com.google.protobuf.GeneratedMessage;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.feed.Feed;
import com.orange.common.utils.DBObjectUtil;
import com.orange.game.model.common.CommonMongoIdComplexListManager;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.service.DBService;
import com.orange.protocol.message.BarrageProtos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by pipi on 15/3/20.
 */
public class MyNewFeedManager extends CommonMongoIdComplexListManager<Feed> {

    private static MyNewFeedManager ourInstance = new MyNewFeedManager();

    public static MyNewFeedManager getInstance() {
        return ourInstance;
    }
    private MyNewFeedManager() {
        super(BarrageConstants.T_MY_NEW_FEED, BarrageConstants.T_FEED, BarrageConstants.F_FEED_ID, Feed.class);
        this.useObjectIdForListKey = false;
    }

    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List invokeOldGetList(String userId, int offset, int limit) {
        return null;
    }

    @Override
    protected List invokeOldGetListForConstruct(String key) {
        return null;
    }

    public void insertMyNewFeed(String userId, BarrageProtos.PBMyNewFeed myNewFeed, int newMessageCount){

        BasicDBObject obj = CommonData.pbToDBObject(myNewFeed);
        insertObject(userId, obj, BarrageConstants.F_MODIFY_DATE, false, false);

        if (newMessageCount > 0){
            // TODO send push message
            pushMessage(userId, myNewFeed);
        }
    }

    public void updateMyNewFeed(String userId, BarrageProtos.PBMyNewFeed myNewFeed, int newMessageCount) {

        BasicDBObject obj = CommonData.pbToDBObject(myNewFeed);
        BasicDBObject incValue = new BasicDBObject();
        if (newMessageCount > 0){
            incValue.put(BarrageConstants.F_COUNT, newMessageCount);
        }

        // remove feedId for update
        obj.removeField(BarrageConstants.F_FEED_ID);

        updateIndexObject(userId, myNewFeed.getFeedId(), obj, incValue);

        if (newMessageCount > 0){
            // TODO send push message
            pushMessage(userId, myNewFeed);
        }
    }

    private void pushMessage(String userId, BarrageProtos.PBMyNewFeed myNewFeed) {

        log.info("<pushMessage> user="+userId);

        // TODO send push
    }


    public List<BarrageProtos.PBMyNewFeed> getUserNewFeed(String userId) {

        List<BarrageProtos.PBMyNewFeed> list = new ArrayList<BarrageProtos.PBMyNewFeed>();

        BasicDBObject obj = getObject(userId, new BasicDBObject());
        if (obj == null){
            log.info("<getUserNewFeed> but obj not found for userId "+userId);
            return list;
        }

        BasicDBList newFeedList = (BasicDBList)obj.get(getListFiledName());
        if (newFeedList == null){
            log.info("<getUserNewFeed> but obj list not exist for userId "+userId);
            return list;
        }

        List<MyNewFeed> feedObjList = new ArrayList<MyNewFeed>();
        for (Object newFeedObj : newFeedList){
            feedObjList.add(new MyNewFeed((DBObject) newFeedObj));
        }

        if (feedObjList.size() == 0){
            log.info("<getUserNewFeed> but obj list size=0 for userId "+userId);
            return list;
        }

        List<MyNewFeed> zeroCountList = new ArrayList<MyNewFeed>();
        for (MyNewFeed myNewFeed : feedObjList){
            if (myNewFeed.getCount() <= 0){
                zeroCountList.add(myNewFeed);
            }
        }

        // remove count is 0 items
        feedObjList.removeAll(zeroCountList);

        // sort by modify date
        Collections.sort(feedObjList, new MyNewFeedDateComparator());

        return MyNewFeed.listToPB(feedObjList, null);
    }

    public void clearMyFeedCount(String userId, String feedId) {

        BasicDBObject updateData = new BasicDBObject();
        updateData.put(BarrageConstants.F_COUNT, 0);

        updateIndexObject(userId, feedId, updateData);
    }
}
