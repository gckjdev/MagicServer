package com.orange.game.model.manager.tutorial;

import com.mongodb.BasicDBObject;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.service.DBService;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-8-15
 * Time: 下午2:41
 * To change this template use File | Settings | File Templates.
 */
public class TutorialStatManager extends CommonManager {

    public static final String ADD_TUTORIAL = "add";
    public static final String PRACTICE_DRAW = "practice";
    public static final String CONQUER_DRAW = "conquer";
    public static final String SUBMIT_CONQUER_DRAW = "submit";

    public static final String PASS_CONQUER_DRAW = "pass";

    private static TutorialStatManager ourInstance = new TutorialStatManager();

    public static TutorialStatManager getInstance() {
        return ourInstance;
    }

    private TutorialStatManager() {
    }

    public void insertTutorialAction(final String userId,
                                     final String tutorialId,
                                     final String stageId,
                                     final int score,
                                     final String actionType){
        if (StringUtil.isEmpty(actionType)){
            return;
        }

        if (StringUtil.isEmpty(tutorialId)){
            return;
        }

        if (StringUtil.isEmpty(stageId)){
            return;
        }

        final String tableName = DBConstants.T_STAT_TUTORIAL_DAILY;
        final String key = DateUtil.dateToChineseStringByFormat(new Date(), "yyyyMMdd");
        final BasicDBObject query = new BasicDBObject("_id", key);
        final BasicDBObject updateValue = new BasicDBObject();
        final BasicDBObject incValue = new BasicDBObject();

        String incCountKey = null;

        if (actionType.equalsIgnoreCase(ADD_TUTORIAL)){
            incCountKey = tutorialId + "." + ADD_TUTORIAL;
            updateValue.put("$inc", new BasicDBObject(incCountKey, 1));
        }
        else if (actionType.equalsIgnoreCase(PRACTICE_DRAW)){

            // tutorial
            incCountKey = tutorialId + "." + PRACTICE_DRAW;
            incValue.put(incCountKey, 1);

            // stage
            incCountKey = tutorialId + "-" + stageId + "." + PRACTICE_DRAW;
            incValue.put(incCountKey, 1);

            updateValue.put("$inc", incValue);
        }
        else if (actionType.equalsIgnoreCase(CONQUER_DRAW)){

            // tutorial
            incCountKey = tutorialId + "." + CONQUER_DRAW;
            incValue.put(incCountKey, 1);

            // stage
            incCountKey = tutorialId + "-" + stageId + "." + CONQUER_DRAW;
            incValue.put(incCountKey, 1);

            updateValue.put("$inc", incValue);
        }
        else if (actionType.equalsIgnoreCase(SUBMIT_CONQUER_DRAW)){

            // tutorial
            incCountKey = tutorialId + "." + SUBMIT_CONQUER_DRAW;
            incValue.put(incCountKey, 1);

            // stage
            incCountKey = tutorialId + "-" + stageId + "." + SUBMIT_CONQUER_DRAW;
            incValue.put(incCountKey, 1);

            if (score >= 60){

                // tutorial
                incCountKey = tutorialId + "." + PASS_CONQUER_DRAW;
                incValue.put(incCountKey, 1);

                // stage
                incCountKey = tutorialId + "-" + stageId + "." + PASS_CONQUER_DRAW;
                incValue.put(incCountKey, 1);
            }

            updateValue.put("$inc", incValue);
        }

        if (updateValue.size() > 0){
            DBService.getInstance().executeDBRequest(1, new Runnable() {
                @Override
                public void run() {
                    log.info("<insertTutorialAction> "+tableName+" query="+query + " update="+updateValue.toString());
                    mongoClient.upsertAll(tableName, query, updateValue);
                }
            });

            DBService.getInstance().executeDBRequest(2, new Runnable() {
                @Override
                public void run() {
                    BasicDBObject query = new BasicDBObject("_id", tutorialId);
                    String tableName = DBConstants.T_STAT_TUTORIAL_ALL;
                    log.info("<insertTutorialAction> "+tableName+" query="+query + " update="+updateValue.toString());
                    mongoClient.upsertAll(tableName, query, updateValue);
                }
            });

        }


    }
}
