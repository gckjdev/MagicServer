package com.orange.game.model.manager.group;

import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.model.dao.bbs.*;
import com.orange.game.model.manager.group.index.GroupIndexManager;
import org.bson.types.ObjectId;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-29
 * Time: 上午10:57
 * To change this template use File | Settings | File Templates.
 */
public class GroupStatManager {


    final private static double DRAW_MAX_SCORES = 20;
    final private static double TEXT_MAX_SCORES = 10;
    final private static double TEXT_BASE_SCORES = 1;
    final private static double DRAW_WEIGHT = 0.0001;  // 1 score 10000 byte.
    final private static double TEXT_WEIGHT = 0.05;  // 1 score 20 words.
    final private static double IMAGE_WEIGHT = 1;              // 1 score an image
    final private static double FLOWER_WEIGHT = 1;              // 1 score a flower
    final private static double CHAT_MESSAGE_WEIGHT = 0.1;              // 0.1 a chat message

    private static int EVENT_CREATION = 1;


    public static void didCreatedTopic(String groupId, BBSPost topic) {
        //increase fame/active...
//        double fame = calculateFame(topic, EVENT_CREATION);
//        GroupIndexManager.fameManager().updateScore(groupId, fame);

        double active = calculateActive(topic, EVENT_CREATION);
        GroupIndexManager.activeManager().updateScore(groupId, active);
    }

    public static void didCreatedAction(String groupId, BBSAction action) {
        double active = calculateActive(action, EVENT_CREATION);
        GroupIndexManager.activeManager().increaseScore(groupId, active);
    }

    private static double calActiveScore(BBSContent content) {
        if (content == null) return 0;

        String text = content.getText();
        boolean hasImage = content instanceof BBSImageContent;

        byte[] data = null;
        if (content instanceof BBSDrawContent) {
            data = ((BBSDrawContent) content).getDrawData();
        }

        double score = 0;
        if (hasImage) score += IMAGE_WEIGHT;
        if (text != null) {
            double textScore = TEXT_BASE_SCORES + text.length() * TEXT_WEIGHT;
            score += Math.min(textScore, TEXT_MAX_SCORES);
        }
        if (data != null) {
            double drawScore = data.length * DRAW_WEIGHT;
            score += Math.min(drawScore, DRAW_MAX_SCORES);

        }
        return score;
    }

    private static double calculateActive(BBSPost topic, int event) {
        return calActiveScore(topic.getContent());
    }

    private static double calculateActive(BBSAction action, int event) {
        return calActiveScore(action.getContent());
    }

    private static double calculateFame(BBSPost topic, int event) {
        return 2;
    }

    private static double calculateFame(BBSAction action, int event) {
        return 1;
    }

    public static void didThrowFlower(MongoDBClient mongoClient, String opusCreatorUid) {
        ObjectId gId = GroupManager.getGroupIdByUserId(mongoClient, opusCreatorUid);
        if (gId != null){
            GroupIndexManager.fameManager().increaseScore(gId.toString(), FLOWER_WEIGHT);
        }
    }

    public static void didSendGroupMessage(String groupId, String text, byte[] drawData, int messageType) {
        if (groupId != null){
            GroupIndexManager.activeManager().increaseScore(groupId, CHAT_MESSAGE_WEIGHT);
        }
    }
}
