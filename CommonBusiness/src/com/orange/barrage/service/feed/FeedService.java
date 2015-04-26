package com.orange.barrage.service.feed;

import com.googlecode.protobuf.format.JsonFormat;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.orange.barrage.common.CommonModelService;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.feed.Feed;
import com.orange.barrage.model.feed.FeedAction;
import com.orange.barrage.model.feed.UserTimelineFeedManager;
import com.orange.barrage.model.feed.index.MyFeedIndexManager;
import com.orange.barrage.model.feed.index.MyNewFeedManager;
import com.orange.barrage.model.feed.index.MyRelatedFeedIndexManager;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.model.DrawProtos;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.ErrorProtos;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pipi on 14/12/8.
 */
public class FeedService extends CommonModelService {
    private static FeedService ourInstance = new FeedService();

    public static FeedService getInstance() {
        return ourInstance;
    }

    private FeedService() {
    }

    public void insertUserTimelines(String userId, List<UserProtos.PBUser> toUserList, String feedId){
        // add index tables
        Set<String> insertUserList = new HashSet<String>();
        insertUserList.add(userId);
        if (toUserList != null) {
            for (UserProtos.PBUser toUser : toUserList) {
                String toUserId = toUser.getUserId();
                insertUserList.add(toUserId);
            }
        }

        for (String toUserId : insertUserList) {
            UserTimelineFeedManager.getInstance().insertUserFeed(toUserId, feedId.toString(), "");
        }

    }

    public int createFeed(BarrageProtos.PBFeed feed, MessageProtos.PBCreateFeedResponse.Builder rspBuilder) {

        String userId = feed.getCreateUser().getUserId();
        List<UserProtos.PBUser> toUserList = feed.getToUsersList();

        DBObject obj = Feed.pbToDBObject(feed, true, BarrageConstants.F_FEED_ID);
        String feedId = obj.get("_id").toString();

        mongoDBClient.insert(BarrageConstants.T_FEED, obj);

        insertUserTimelines(userId, toUserList, feedId.toString());

        // insert into index for notification
        BarrageProtos.PBFeed.Builder builder = BarrageProtos.PBFeed.newBuilder(feed);
        builder.setFeedId(feedId.toString());
        updateUserNewFeedForCreation(builder.build());

        rspBuilder.setFeedId(feedId.toString());
        return 0;
    }

    public int deleteFeedAction(String feedId, String actionId, MessageProtos.PBDeleteFeedActionResponse.Builder rspBuilder) {

        if (StringUtil.isEmpty(feedId)){
            return ErrorProtos.PBError.ERROR_FEED_ID_NULL_VALUE;
        }

        if (StringUtil.isEmpty(actionId)){
            return ErrorProtos.PBError.ERROR_FEED_ACTION_ID_NULL_VALUE;
        }

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(feedId));

        BasicDBObject pullValue = new BasicDBObject();
        pullValue.put(BarrageConstants.F_ACTIONS, new BasicDBObject(BarrageConstants.F_ACTION_ID, actionId));

        BasicDBObject pull = new BasicDBObject("$pull", pullValue);

        log.info("<deleteFeedAction> query="+query.toString()+", pull="+pull.toString());
        mongoDBClient.updateAll(BarrageConstants.T_FEED, query, pull);

        return 0;
    }

    public int replyFeed(BarrageProtos.PBFeedAction action, MessageProtos.PBReplyFeedResponse.Builder rspBuilder) {

        String feedId = action.getFeedId();

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(feedId));

        DBObject obj = FeedAction.pbToDBObject(action, true, BarrageConstants.F_ACTION_ID);
        String actionId = (String)obj.get(BarrageConstants.F_ACTION_ID);

        if (obj == null || obj.keySet().size() == 0){
            return ErrorProtos.PBError.ERROR_FEED_ACTION_INVALID_VALUE;
        }

        // warning here!!! we must delete actionID _id field !!!
        obj.removeField("_id");

        DBObject pushUpdate = new BasicDBObject();
        pushUpdate.put(BarrageConstants.F_ACTIONS, obj);

        BasicDBObject update = new BasicDBObject();
        update.put("$push", pushUpdate);

        DBObject feedObj = mongoDBClient.findAndModify(BarrageConstants.T_FEED, query, update);

        // insert into related index tables
        BarrageProtos.PBFeedAction.Builder actionBuilder = BarrageProtos.PBFeedAction.newBuilder(action);
        actionBuilder.setActionId(actionId);
        Feed feed = new Feed(feedObj);
        BarrageProtos.PBFeed pbFeed = feed.toProtoBufModel();
        updateUserNewFeedForAction(actionBuilder.build(), pbFeed);

        CommonData dataObj = new CommonData(obj);
        BarrageProtos.PBFeedAction.Builder retFeedActionBuilder = BarrageProtos.PBFeedAction.newBuilder();
        dataObj.toPB(retFeedActionBuilder, null);
        rspBuilder.setAction(retFeedActionBuilder.build());
        return 0;
    }

    public int deleteFeed(String feedId, MessageProtos.PBDeleteFeedResponse.Builder rspBuilder, String createUserId) {
        if (StringUtil.isEmpty(feedId)){
            return ErrorProtos.PBError.ERROR_FEED_ID_NULL_VALUE;
        }

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(feedId));

        log.info("<deleteFeed> query="+query.toString());
        mongoDBClient.remove(BarrageConstants.T_FEED, query);

        // TODO get createUserId from feed data...

        // remove index
        MyFeedIndexManager.getInstance().removeFeed(createUserId, feedId);

        return 0;
    }

    public int getUserNewFeed(String userId, MessageProtos.PBGetMyNewFeedListResponse.Builder rspBuilder) {

        if (StringUtil.isEmpty(userId)){
            log.warn("<getUserNewFeed> but userId empty or null");
            return ErrorProtos.PBError.ERROR_INCORRECT_INPUT_DATA_VALUE;
        }

        List<BarrageProtos.PBMyNewFeed> list = MyNewFeedManager.getInstance().getUserNewFeed(userId);
        BarrageProtos.PBMyNewFeedList.Builder builder = BarrageProtos.PBMyNewFeedList.newBuilder();
        builder.addAllMyFeeds(list);

        rspBuilder.setMyNewFeedList(builder);
        return 0;
    }

    public void updateUserNewFeedForCreation(BarrageProtos.PBFeed pbFeed){

        String createUserId = pbFeed.getCreateUser().getUserId();
        String feedId = pbFeed.getFeedId();

        // 插入用户自己发表的图片索引表
        MyFeedIndexManager.getInstance().insertFeed(createUserId, feedId);

        // 插入到自己的新消息流表，用于初始化，无需新增消息计数
        BarrageProtos.PBMyNewFeed.Builder builder = BarrageProtos.PBMyNewFeed.newBuilder();
        builder.setFeedId(feedId);
        builder.setType(BarrageProtos.PBMyNewFeedType.TYPE_COMMENT_MY_FEED_VALUE);
        builder.setCount(0);
        builder.setMDate(DateUtil.getCurrentSeconds());
        builder.setIsRead(true);
        builder.setImage(pbFeed.getImage());
        builder.setUserId(createUserId);
        MyNewFeedManager.getInstance().insertMyNewFeed(createUserId, builder.build(), 0);

        // 插入到分享相关的用户的新消息流表
        for (UserProtos.PBUser toUser : pbFeed.getToUsersList()){
            if (!toUser.getUserId().equalsIgnoreCase(createUserId)){
                BarrageProtos.PBMyNewFeed.Builder toBuilder = BarrageProtos.PBMyNewFeed.newBuilder();
                toBuilder.setFeedId(feedId);
                toBuilder.setType(BarrageProtos.PBMyNewFeedType.TYPE_NEW_FEED_TO_ME_VALUE);  // 发给我的
                toBuilder.setCount(1);
                toBuilder.setMDate(DateUtil.getCurrentSeconds());
                toBuilder.setIsRead(false);
                toBuilder.setImage(pbFeed.getImage());
                toBuilder.setUserId(createUserId);

                MyNewFeedManager.getInstance().insertMyNewFeed(toUser.getUserId(), toBuilder.build(), 1);
            }
        }

    }

    public void updateUserNewFeedForAction(BarrageProtos.PBFeedAction pbFeedAction, BarrageProtos.PBFeed pbFeed){
        String actionUserId = pbFeedAction.getUser().getUserId();
        String feedId = pbFeedAction.getFeedId();
        String actionId = pbFeedAction.getActionId();
        String createUserId = pbFeed.getCreateUser().getUserId();

        // 插入到我参与的图片流
        if (!actionUserId.equalsIgnoreCase(createUserId)) {

            // TODO can be optimized by check feed action info to avoid duplicate insert
            MyRelatedFeedIndexManager.getInstance().insertFeed(actionUserId, feedId);

            // 更新作者的新消息流
            BarrageProtos.PBMyNewFeed.Builder builder = BarrageProtos.PBMyNewFeed.newBuilder();
            builder.setFeedId(feedId);
            builder.setActionId(actionId);
            builder.setType(BarrageProtos.PBMyNewFeedType.TYPE_COMMENT_MY_FEED_VALUE);
            builder.setMDate(DateUtil.getCurrentSeconds());
            builder.setIsRead(false);
            builder.setImage(pbFeed.getImage());
            builder.setUserId(createUserId);
            MyNewFeedManager.getInstance().updateMyNewFeed(createUserId, builder.build(), 1);
        }

        // 更新当前评论过用户（根据 feed 的信息）的新消息流
        if (pbFeed.getActionsList() != null){
            Set<String> otherUserIds = new HashSet<String>();
            for (BarrageProtos.PBFeedAction feedAction : pbFeed.getActionsList()){
                String otherUserId = feedAction.getUser().getUserId();
                if (otherUserId != null && !actionUserId.equalsIgnoreCase(otherUserId)){
                    otherUserIds.add(otherUserId);
                }
            }

            log.info("<updateUserNewFeedForAction> MY_INVOLVED_COMMENT user ID list="+otherUserIds.toString());
            for (String otherUserId : otherUserIds){
                // 更新评论同一图片的其他用户的新消息流
                BarrageProtos.PBMyNewFeed.Builder builder = BarrageProtos.PBMyNewFeed.newBuilder();
                builder.setFeedId(feedId);
                builder.setActionId(actionId);
                builder.setType(BarrageProtos.PBMyNewFeedType.TYPE_MY_INVOLVED_COMMENT_VALUE);
                builder.setMDate(DateUtil.getCurrentSeconds());
                builder.setIsRead(false);
                builder.setImage(pbFeed.getImage());
                builder.setUserId(actionUserId);
                MyNewFeedManager.getInstance().updateMyNewFeed(otherUserId, builder.build(), 1);
            }
        }
    }

    // TODO
    public int readUserNewFeed(String userId, String feedId) {
        MyNewFeedManager.getInstance().clearMyFeedCount(userId, feedId);
        return 0;
    }

    public int getFeedById(String feedId, MessageProtos.PBGetFeedByIdResponse.Builder rspBuilder) {
        BasicDBObject query = new BasicDBObject("_id", new ObjectId(feedId));
        DBObject obj = mongoDBClient.findOne(BarrageConstants.T_FEED, query);
        if (obj == null){
            return ErrorProtos.PBError.ERROR_FEED_NOT_FOUND_VALUE;
        }

        Feed feed = new Feed(obj);
        BarrageProtos.PBFeed pbFeed = feed.toProtoBufModel();

        rspBuilder.setFeed(pbFeed);
        return 0;
    }

    public int getUserFeed(String userId, int offset, int limit, MessageProtos.PBGetUserFeedResponse.Builder rspBuilder) {

        List<Feed> list = MyFeedIndexManager.getInstance().getUserFeedList(userId, offset, limit);
        List<BarrageProtos.PBFeed> pbFeedList = Feed.listToPB(list, null);

        if (pbFeedList != null) {
            rspBuilder.addAllFeeds(pbFeedList);
        }

        return 0;
    }
}
