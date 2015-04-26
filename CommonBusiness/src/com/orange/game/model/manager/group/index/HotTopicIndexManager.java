package com.orange.game.model.manager.group.index;

import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.service.DBService;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by gamy on 14-2-9.
 */
public class HotTopicIndexManager extends CommonZSetIndexManager<BBSPost> {

    private HotTopicIndexManager() {
        super("hot_topic", DBConstants.T_GROUP_TOPIC, 1000, BBSPost.class);
    }

    private static final HotTopicIndexManager instance = new HotTopicIndexManager();

    public final static HotTopicIndexManager getInstance() {
        return instance;
    }

    @Override
    public void updateTopScore(String id, double score, Callable updateMongoCallable, boolean replaceOnlyHigher, boolean background) {
        super.updateTopScore(id, score, updateMongoCallable, replaceOnlyHigher, background);
    }

    @Override
    public List<ObjectId> getTopIdList(int offset, int limit) {
        return super.getTopIdList(offset, limit);
    }


    public void updateTopicScore(final String postId) {

        final double score = calculateScore(postId);

        this.updateTopScore(postId, score, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        }, false, true);
    }

    private double calculateScore(String postId) {
        BBSPost post = BBSManager.getBBSPostByPostId(DBService.getInstance().getMongoDBClient(), postId, BBSManager.MODE_GROUP);
        return post.hotScore();
    }


    public List<BBSPost> getTopList(int offset, int limit) {
        return getTopList(offset, limit, DBConstants.F_STATUS, BBSPost.StatusDelete, null);
    }

    public void removeTopicId(String topicId) {
        removeMember(topicId);
    }
}
