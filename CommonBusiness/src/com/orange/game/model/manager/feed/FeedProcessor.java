package com.orange.game.model.manager.feed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.orange.game.model.manager.timeline.*;
import com.orange.game.model.service.DBService;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.RelationManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.friend.FriendManager;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;

public class FeedProcessor {
	public static final Logger log = Logger.getLogger(FeedProcessor.class
			.getName());

	// inner class
	private static class ProcessorUnit extends Object {
		protected static final int ProcessTypeAdd = 1;
		protected static final int ProcessTypeDelete = 2;

		private int processType;
		private UserAction action;
		private String appId;

		public ProcessorUnit(int processType, UserAction action,String appId) {
			super();
			this.processType = processType;
			this.action = action;
			this.appId = appId;
		}

		public int getProcessType() {
			return processType;
		}

		public UserAction getaction() {
			return action;
		}

		public String getAppId() {
			return appId;
		}
		
		

	}

	private static MongoDBClient mongoClient = DBService.getInstance().getMongoDBClient();

    private static final int THREAD_NUMBER = 3;
	private final static FeedProcessor instance = new FeedProcessor();
	private final static ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUMBER);

	private FeedProcessor() {
		super();
	}

	public static FeedProcessor getInstance() {
		return instance;
	}
	
	public void setMongoDBClient(MongoDBClient client ){
		mongoClient = client; 
	}

	public void RegistAddProcess(UserAction action,String appId) {
		ProcessorUnit unit = new ProcessorUnit(ProcessorUnit.ProcessTypeAdd,
				action,appId);
		handleUnit(unit);
	}

	public void RegistDeleteProcess(UserAction action,String appId) {
		ProcessorUnit unit = new ProcessorUnit(ProcessorUnit.ProcessTypeDelete,
				action,appId);
		handleUnit(unit);
	}

	private List<ObjectId> getFansIds(String userID) {
		if (userID == null) {
			return Collections.emptyList();
		}
		List<ObjectId>list = RelationManager.getAllFanUids(mongoClient, userID);
        // rem by Benson 2013-09-11 to fix the incorrect timeline bugs
        // FriendManager.getFriendfansmanager().getUserFriendIdList(userID, 0, Integer.MAX_VALUE);
		if (list == null) {
			list = new ArrayList<ObjectId>();
		}
		list.add(new ObjectId(userID));
		return list;
	}

	private void pushAction(String actionId, String creator, List<ObjectId> uidList,String appId,int actionType, boolean insertCreator) {
		if (StringUtil.isEmpty(actionId) || uidList == null || StringUtil.isEmpty(creator) || uidList.size() == 0) {
			return;
		}

		// TODO Due to we can not upsert the ower filed into the table. So, use
		// the stupid method. improve it later. By Gamy

        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);

        OpusTimelineByCategoryManager opusTimelineManager = xiaoji.opusTimelineByCategoryManager();
        GuessOpusTimelineByCategoryManager guessOpusTimelineManager = xiaoji.guessOpusTimelineByCategoryManager();

        OpusTimelineManager allOpusTimelineManager = xiaoji.opusTimelineManager();
        GuessOpusTimelineManager allGuessOpusTimelineManager = xiaoji.guessOpusTimelineManager();

//		for (ObjectId oid : uidList) {

//            DBObject query = new BasicDBObject();
//			query.put(DBConstants.F_OWNER, oid);
//			DBObject update = new BasicDBObject();
//
//			DBObject pushUpdate = new BasicDBObject();
//			pushUpdate.put(DBConstants.F_ACTION_IDS, new ObjectId(actionId));
//			update.put("$push", pushUpdate);
//
//			DBObject incUpdate = new BasicDBObject();
//			incUpdate.put(DBConstants.F_TIMELINE_COUNT, 1);
//			if (oid.toString().equalsIgnoreCase(creator)) {
//				incUpdate.put(DBConstants.F_TIMELINE_READ_COUNT, 1);
//			}
//			update.put("$inc", incUpdate);
//			mongoClient.upsertAll(DBConstants.T_TIMELINE, query, update);

//            boolean isUpdateNewCount = (oid.toString().equalsIgnoreCase(creator) == false);
//
//            if (UserAction.isOpus(actionType)){
//                //insert opus timeline index
//                opusTimelineManager.insertId(oid.toString(), actionId, isUpdateNewCount, false);
//
//                //insert all timeline also
//                allOpusTimelineManager.insertId(oid.toString(), actionId, isUpdateNewCount, false);
//            }
//            else if (UserAction.isGuess(actionType)){
//                guessOpusTimelineManager.insertId(oid.toString(), actionId, isUpdateNewCount, false);
//                allGuessOpusTimelineManager.insertId(oid.toString(), actionId, isUpdateNewCount, false);
//            }
//            else{
//                log.warn("<pushAction> but action type "+actionType+" is neither opus nor guess");
//            }
//		}

        // add into user self timeline
        if (insertCreator){
            if (UserAction.isLearnDraw(actionType)){
                TutorialTimelineManager.getInstance().insertOwnerUserTimeline(creator, actionId);
            }
            else if (UserAction.isOpus(actionType)){
                //insert opus timeline index
                opusTimelineManager.insertId(creator, actionId, false, false);

                //insert all timeline also
                allOpusTimelineManager.insertId(creator, actionId, false, false);
            }
            else if (UserAction.isGuess(actionType)){
                guessOpusTimelineManager.insertId(creator, actionId, false, false);
                allGuessOpusTimelineManager.insertId(creator, actionId, false, false);
            }
            else{
                log.warn("<pushAction> but action type "+actionType+" is neither opus nor guess");
            }
        }

        // add into user all fans timeline
        uidList.remove(new ObjectId(creator));
        log.info("<addUserFansTimeline> begin actionId=" + actionId);
        if (UserAction.isLearnDraw(actionType)){
            // for user friends timeline insertion
            TutorialTimelineManager.getInstance().insertIdIntoOwnerIdList(uidList, actionId, true, 1, true, false);
        }
        else if (UserAction.isOpus(actionType)){
            //insert opus timeline index
            opusTimelineManager.insertIdIntoOwnerIdList(uidList, actionId, true, 1, true, false);

            //insert all timeline also
            allOpusTimelineManager.insertIdIntoOwnerIdList(uidList, actionId, true, 1, true, false);
        }
        else if (UserAction.isGuess(actionType)){
            guessOpusTimelineManager.insertIdIntoOwnerIdList(uidList, actionId, true, 1, true, false);
            allGuessOpusTimelineManager.insertIdIntoOwnerIdList(uidList, actionId, true, 1, true, false);
        }
        else{
            log.warn("<pushAction> but action type "+actionType+" is neither opus nor guess");
        }
        log.info("<addUserFansTimeline> end actionId="+actionId);

	}

	private void pullAction(String action, List<ObjectId> uidList) {

        if (uidList.size() == 0)
            return;

		DBObject query = new BasicDBObject();
		DBObject inQuery = new BasicDBObject();
		inQuery.put("$in", uidList);
		query.put(DBConstants.F_OWNER, inQuery);

		DBObject update = new BasicDBObject();
		DBObject pushUpdate = new BasicDBObject();
		pushUpdate.put(DBConstants.F_ACTION_IDS, new ObjectId(action));
		update.put("$pull", pushUpdate);
		
		DBObject incUpdate = new BasicDBObject();
		incUpdate.put(DBConstants.F_TIMELINE_COUNT, -1);
		update.put("$inc", incUpdate);
		
		mongoClient.updateAll(DBConstants.T_TIMELINE, query, update);
	}

	private void handleUnit(final ProcessorUnit unit) {
		log.info("handle unit.");
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (unit != null && unit.getaction() != null) {
						UserAction action = unit.getaction();
						String appId = unit.getAppId();
                        List<ObjectId> uidList = getFansIds(action.getCreateUserId());
                        if (uidList == null || uidList.size() == 0){
                            return;
                        }
                        int total = uidList.size();
                        int INSERT_PER_TIME = 500;
                        int splits = total / INSERT_PER_TIME + 1;
                        for (int i=0; i<splits; i++){

                            int startIndex = i*INSERT_PER_TIME;                 // start, include in sublist
                            int endIndex = startIndex + INSERT_PER_TIME;        // end, not include in sublist
                            if (startIndex < 0 || startIndex >= total){
                                break;
                            }

                            if (endIndex >= total){
                                endIndex = total;
                            }

                            List<ObjectId> subList = new ArrayList<ObjectId>(uidList.subList(startIndex, endIndex));
                            if (subList.size() == 0){
                                break;
                            }

                            boolean insertForCreator = (i == 0);

                            log.info("<addActionIntoFans> from "+startIndex+" to "+endIndex);
                            if (unit.getProcessType() == ProcessorUnit.ProcessTypeAdd) {
                                pushAction(action.getActionId(), action.getCreateUserId(), subList,appId,action.getType(), insertForCreator);

                            } else if (unit.getProcessType() == ProcessorUnit.ProcessTypeDelete) {
                                pullAction(action.getActionId(), subList);
                            }
                        }
					}

				} catch (Exception e) {
					log.error("<FeedProcessor>Exception: e="+e.toString(), e);
				}
			}
		});
	}

}
