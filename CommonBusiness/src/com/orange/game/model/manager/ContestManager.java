package com.orange.game.model.manager;

import java.awt.Cursor;
import java.util.*;

import com.mongodb.BasicDBList;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.common.IntKeyValue;
import com.orange.game.model.dao.common.UserAward;
import com.orange.game.model.manager.group.FollowGroupManager;
import com.orange.game.model.service.DBService;
import com.orange.network.game.protocol.constants.GameConstantsProtos;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GroupProtos;
import org.apache.commons.lang.time.DateUtils;
import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.dao.Contest.ContestLimit;

public class ContestManager extends CommonManager {

    private static List<Contest> ongoingAnouymousContestList;

    private static DBObject getContentReturnFields(boolean hasWordList,
			boolean hasParticipantList) {
		DBObject object = new BasicDBObject();
		if (!hasWordList) {
			object.put(DBConstants.F_WORD_LIST, 0);
		}
		if (!hasParticipantList) {
			object.put(DBConstants.F_PARTICIPANT_LIST, 0);
            object.put(DBConstants.F_OPUS_LIST, 0);			// don't return opus list
		}

		return object;
	}

    private static List<Contest> getContestList(MongoDBClient mongoClient,
                                                DBObject query, DBObject returnFields, int offset, int limit)
    {
        return getContestList(mongoClient, query, returnFields, offset, limit, "_id");
    }

	private static List<Contest> getContestList(MongoDBClient mongoClient,
			DBObject query, DBObject returnFields, int offset, int limit, String sortField) {
		DBObject orderBy = new BasicDBObject();
		orderBy.put(sortField, -1);
        log.info("<getContestList> query="+query.toString());
		DBCursor cursor = mongoClient.find(DBConstants.T_CONTEST, query,
				returnFields, orderBy, offset, limit);
		if (cursor != null) {
			List<Contest> contestList = new ArrayList<Contest>();
			while (cursor.hasNext()) {
				DBObject object = cursor.next();
				if (object != null) {
					Contest contest = new Contest(object);
					contestList.add(contest);
				}
			}
            cursor.close();
			if (!contestList.isEmpty()) {
				return contestList;
			}
		}
		return Collections.emptyList();
	}

	// private static DBObject getTimeRangeQuery
    private static List<Contest> getContestList(
            MongoDBClient mongoClient, int offset, int limit, int language, int status) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_LANGUAGE, language);
        query.put(DBConstants.F_STATUS, status);
        DBObject returnFields = getContentReturnFields(false, false);
        return getContestList(mongoClient, query, returnFields, offset, limit);
    }

    private static List<Contest> getContestList(
            MongoDBClient mongoClient, int offset, int limit, int language, int status, int category) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_LANGUAGE, language);
        query.put(DBConstants.F_STATUS, status);
        query.put(DBConstants.F_CATEGORY, category);
//        query.put(DBConstants.F_IS_GROUP, false);
        DBObject returnFields = getContentReturnFields(false, false);
        return getContestList(mongoClient, query, returnFields, offset, limit);
    }



	public static List<Contest> getCurrentContestList(
			MongoDBClient mongoClient, int offset, int limit, int language, int category) {
        return getContestList(mongoClient, offset, limit, language, GameConstantsProtos.PBContestStatus.Running_VALUE, category);
	}

    public static List<Contest> getCurrentContestList(
            MongoDBClient mongoClient, int offset, int limit, int language) {
        return getContestList(mongoClient, offset, limit, language, GameConstantsProtos.PBContestStatus.Running_VALUE);
    }


    public static List<Contest> getAllCurrentContestList(
            MongoDBClient mongoClient, int offset, int limit, int language) {
        return getContestList(mongoClient, offset, limit, language, GameConstantsProtos.PBContestStatus.Running_VALUE);
    }

	public static List<Contest> getPendingContestList(
			MongoDBClient mongoClient, int offset, int limit, int language, int category) {
		DBObject query = new BasicDBObject();
		DBObject startTime = new BasicDBObject();
		startTime.put("$gt", new Date());
		query.put(DBConstants.F_START_DATE, startTime);
		query.put(DBConstants.F_LANGUAGE, language);
        query.put(DBConstants.F_CATEGORY, category);
        query.put(DBConstants.F_IS_GROUP, false);
        DBObject returnFields = getContentReturnFields(false, false);
		return getContestList(mongoClient, query, returnFields, offset, limit);
	}

	public static List<Contest> getPassedContestList(MongoDBClient mongoClient,
			int offset, int limit, int language, int category) {
		DBObject query = new BasicDBObject();
		DBObject endTime = new BasicDBObject();
		endTime.put("$lt", new Date());
		query.put(DBConstants.F_END_DATE, endTime);
		DBObject returnFields = getContentReturnFields(false, false);
		query.put(DBConstants.F_LANGUAGE, language);
        query.put(DBConstants.F_CATEGORY, category);
        query.put(DBConstants.F_IS_GROUP, false);
        return getContestList(mongoClient, query, returnFields, offset, limit);
	}

	public static List<Contest> getMyContest(MongoDBClient mongoClient,
			String userId, int offset, int limit) {
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_PARTICIPANT_LIST, userId);
		DBObject returnFields = getContentReturnFields(false, false);
		return getContestList(mongoClient, query, returnFields, offset, limit);
	}

	public static Contest getContestById(MongoDBClient mongoClient,
			String contestId) {
		if (StringUtil.isEmpty(contestId)) {
			return null;
		}
		DBObject object = mongoClient.findOne(DBConstants.T_CONTEST,
                "_id", new ObjectId(contestId), getContentReturnFields(false, false));
		if (object == null) {
			return null;
		}
		return new Contest(object);
	}

    public static Contest getContestByIdWithAllData(MongoDBClient mongoClient,
                                         String contestId) {
        if (StringUtil.isEmpty(contestId)) {
            return null;
        }
        DBObject object = mongoClient.findOne(DBConstants.T_CONTEST,
                "_id", new ObjectId(contestId), getContentReturnFields(true, true));
        if (object == null) {
            return null;
        }
        return new Contest(object);
    }

	private static Date dateDistanceNow(int days) {
		Date date = new Date();
		long time = date.getTime() + days * 24 * 3600 * 1000;
		date.setTime(time);
		return date;
	}

	private static List<Contest> addNewContest(MongoDBClient mongoClient) {
		int count = 1;
		log.info("add new contests");
		List<Contest> list = new ArrayList<Contest>();
		for (int i = 0; i < count; i++) {
			Contest contest = new Contest(new BasicDBObject());
			contest.setOpusCount(0);
			contest.setParticipantCount(0);
			contest.setLanguage(1);
			Date startDate = null;
			Date endDate = null;
			if (i == 0) {
				contest.setType(Contest.CONTEST_TYPE_TOPIC);
				contest.setTitle("冬日美食大赛");
				contest
						.setContestUrl("http://58.215.184.18:8080/contest/image/888888888888888888890000_contest_iphone.jpg");
				contest
						.setStatementUrl("http://58.215.184.18:8080/contest/image/888888888888888888890000_rule_iphone.jpg");
				// startDate = dateDistanceNow(0);
				// endDate = dateDistanceNow(7);
				startDate = DateUtil.dateFromString("20121117000000");
				endDate = DateUtil.dateFromString("20121126000000");
				// yyyyMMddHHmmss
				// yyyyMMddHHmmss
				contest.setSummitCount(1);
				contest.setLanguage(1);
			} else if (i == 1) {
				contest.setType(Contest.CONTEST_TYPE_TOPIC);
				contest.setTitle("猜猜画画10月赛");
				contest.setContestUrl("http://weibo.cn");
				contest.setStatementUrl("http://happy.smslt.com/qq/hdgz.htm");
				startDate = dateDistanceNow(-2);
				endDate = dateDistanceNow(10);
				contest.setSummitCount(3);
				contest.setLanguage(1);
			} else if (i == 2) {
				contest.setType(Contest.CONTEST_TYPE_WORD);
				contest.setTitle("猜猜画画11月赛");
				contest.setContestUrl("http://www.drawlively.com");
				contest.setStatementUrl("http://happy.smslt.com/qq/hdgz.htm");
				startDate = dateDistanceNow(-15);
				endDate = dateDistanceNow(-1);
				contest.setSummitCount(3);
				contest.setLanguage(1);
			} else {
				contest.setType(Contest.CONTEST_TYPE_WORD);
				contest.setTitle("English Contest");
				contest.setContestUrl("http://m.baidu.com");
				contest.setStatementUrl("http://happy.smslt.com/qq/hdgz.htm");
				startDate = dateDistanceNow(-1);
				endDate = dateDistanceNow(10);
				contest.setSummitCount(3);
				contest.setLanguage(2);
			}
			contest.setStartDate(startDate);
			contest.setEndDate(endDate);

			log.info(startDate.getTime());
			log.info(endDate.getTime());

			list.add(contest);
			contest
					.setContestIPadUrl("http://58.215.184.18:8080/contest/image/888888888888888888890000_contest_ipad.jpg");
			contest
					.setStatementIPadUrl("http://58.215.184.18:8080/contest/image/888888888888888888890000_rule_ipad.jpg");
			log.info(contest.getDbObject());
			mongoClient.insert(DBConstants.T_CONTEST, contest.getDbObject());
		}
		return list;
	}

    public static List<Contest> getAllContestList(MongoDBClient mongoClient,
                                                  int offset, int limit, int language) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_LANGUAGE, language);
        DBObject returnFields = getContentReturnFields(false, false);
        List<Contest> list = getContestList(mongoClient, query, returnFields,
                offset, limit);
        return list;
    }

    public static List<Contest> getAllContestList(MongoDBClient mongoClient,
                                                  int offset, int limit, int language, int category) {
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_LANGUAGE, language);
        query.put(DBConstants.F_CATEGORY, category);
        query.put(DBConstants.F_IS_GROUP, false);
		DBObject returnFields = getContentReturnFields(false, false);
		List<Contest> list = getContestList(mongoClient, query, returnFields,
				offset, limit);
		return list;
	}

    public static List<Contest> getAllContestListWithAllData(MongoDBClient mongoClient,
                                                  int offset, int limit, int language) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_LANGUAGE, language);
        DBObject returnFields = getContentReturnFields(true, true);
        List<Contest> list = getContestList(mongoClient, query, returnFields,
                offset, limit);
        // if (list == null) {
        // list = addNewContest(mongoClient);
        // }
        return list;
    }

    public static List<Contest> getTestContestList(MongoDBClient mongoClient,
                                                  int offset, int limit, int language) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_LANGUAGE, language);
        DBObject returnFields = getContentReturnFields(true, true);

        Contest contest = getContestByIdWithAllData(mongoClient, "888888888888888888890007");
        if (contest == null){
            return Collections.emptyList();
        }

        List<Contest> list = new ArrayList<Contest>();
        list.add(contest);
        return list;
    }


    public static void addUserAndOpusToContest(MongoDBClient mongoClient,
			String uid, String actionId, String contestId) {
		DBObject update = new BasicDBObject();
		DBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(contestId));

		// add opus
		DBObject add = new BasicDBObject();
		add.put(DBConstants.F_OPUS_LIST, actionId);
		update.put("$push", add);
		;
		BasicDBObject inc = new BasicDBObject();
		inc.put(DBConstants.F_OPUS_COUNT, 1);
		update.put("$inc", inc);
		mongoClient.updateOne(DBConstants.T_CONTEST, query, update);

		// add uid
		DBObject ne = new BasicDBObject();
		ne.put("$ne", uid);
		query.put(DBConstants.F_PARTICIPANT_LIST, ne);

		inc = new BasicDBObject();
		inc.put(DBConstants.F_PARTICIPANT_COUNT, 1);
		update = new BasicDBObject();
		update.put("$inc", inc);

		add = new BasicDBObject();
		add.put(DBConstants.F_PARTICIPANT_LIST, uid);
		update.put("$push", add);
		mongoClient.updateOne(DBConstants.T_CONTEST, query, update);
	}

    @Deprecated
	public static boolean isContestEnd(MongoDBClient mongoClient,
			String contestId) {
		DBObject query = new BasicDBObject();
		DBObject endTime = new BasicDBObject();
		endTime.put("$lt", DateUtils.addHours(new Date(), 1));
		query.put("_id", new ObjectId(contestId));
		query.put(DBConstants.F_END_DATE, endTime);

		log.info("<isContestEnd> query = " + query);
		long count = mongoClient.count(DBConstants.T_CONTEST, query);

		return count > 0;
	}

    // get all contest related users
    public static Map<String, User> getContestUsers(Contest contest) {

        List<String> userIdList = new ArrayList<String>();

        List<String> judges = contest.getJudgeList();
        if (judges != null){
            userIdList.addAll(judges);
        }

        List<String> contestants = contest.getContestantList();
        if (contestants != null){
            userIdList.addAll(contestants);
        }

        List<String> reporters = contest.getReporterList();
        if (reporters != null){
            userIdList.addAll(reporters);
        }

        List<UserAward> winners = contest.getWinnerList();
        if (winners != null){
            for (UserAward winner : winners){
                if (winner.getUserId() != null){
                    userIdList.add(winner.getUserId());
                }
            }
        }

        List<UserAward> awardList = contest.getAwardResult();
        if (awardList != null){
            for (UserAward award : awardList){
                if (award.getUserId() != null){
                    userIdList.add(award.getUserId());
                }
            }
        }

        // find uses
        List<User> userList = UserManager.findPublicUserInfo(userIdList);

        // put into map
        Map<String, User> map = new HashMap<String, User>();
        if (userList != null){
            for (User user : userList){
                map.put(user.getUserId(), user);
            }
        }
        log.debug("<getContestUser> total "+map.size()+" user returned, user="+map.toString());

        return map;

    }

    public static void updateContestResult(String contestId, BasicDBList winnerList, BasicDBList awardList) {

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(contestId));
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_WINNER_LIST, winnerList);
        updateValue.put(DBConstants.F_AWARD_LIST, awardList);

        updateValue.put(DBConstants.F_STATUS, GameConstantsProtos.PBContestStatus.Passed_VALUE);

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateValue);

        DBService.getInstance().getMongoDBClient().updateAll(DBConstants.T_CONTEST, query, update);
    }

    public static void updateContest(String contestId, BasicDBObject obj) {
        DBService.getInstance().getMongoDBClient().updateAllByDBObject(DBConstants.T_CONTEST, contestId, obj);
    }

    public static Set<String> getOngoingAnouymousContestIds() {
        List<Contest> list = getAllCurrentContestList(DBService.getInstance().getMongoDBClient(), 0, 1000, DBConstants.C_LANGUAGE_CHINESE);
        if (list == null || list.size() == 0){
            log.info("<getOngoingAnouymousContestIds> no data");
            return Collections.emptySet();
        }

        Set<String> retCollection = new HashSet<String>();
        for (Contest contest : list){
            if (contest.getIsAnonymous()){
                retCollection.add(contest.getContestId());
            }
        }

        log.info("<getOngoingAnouymousContestIds> ids="+retCollection.toString());
        return retCollection;
    }

    public static Contest createContest(GroupProtos.PBContest pbContest, String thumbImageUrl, String imageUrl) {

        if (pbContest == null){
            return null;
        }

        Contest contest = new Contest(pbContest, imageUrl);
        log.info("<createContest> contest="+contest.getDbObject().toString());
        DBService.getInstance().getMongoDBClient().insert(DBConstants.T_CONTEST, contest.getDbObject());
        return contest;

    }


    public static List<Contest> getContestListByGroupId(MongoDBClient mongoClient, int offset, int limit, String groupId, int category) {

        if (StringUtil.isEmpty(groupId)){
            return Collections.emptyList();
        }

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_CATEGORY, category);
        query.put(DBConstants.F_GROUPID, groupId);
        DBObject returnFields = getContentReturnFields(false, false);

        return getContestList(mongoClient, query, returnFields, offset, limit);
    }


    public static List<Contest> getAllGroupContestList(MongoDBClient mongoClient, int offset, int limit, int category) {

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_CATEGORY, category);
        query.put(DBConstants.F_IS_GROUP, true);
        DBObject returnFields = getContentReturnFields(false, false);

        return getContestList(mongoClient, query, returnFields, offset, limit);
    }

    public static int updateContestByPBContest(GroupProtos.PBContest pbContest, String imageUrl) {

        if (pbContest == null || !ObjectId.isValid(pbContest.getContestId())){
            return ErrorCode.ERROR_PARAMETER_CONTESTID_EMPTY;
        }

        Contest currentContest = ContestManager.getContestById(DBService.getInstance().getMongoDBClient(),
                pbContest.getContestId());

        if (currentContest == null){
            return ErrorCode.ERROR_CONTEST_NOT_FOUND;
        }

        if ((currentContest.canSubmit() || currentContest.canVote())){
            // cannot change contest after it starts
            return ErrorCode.ERROR_CONTEST_CANNOT_UPDATE_AFTER_START;
        }


        Contest updateContest = new Contest();

        if (!StringUtil.isEmpty(imageUrl)){
            updateContest.setContestIPadUrl(imageUrl);
            updateContest.setContestUrl(imageUrl);
        }

        updateContest.setContestId(pbContest.getContestId());
        updateContest.setTitle(pbContest.getTitle());

        if (pbContest.getStartDate() > 0){
            updateContest.setStartDate(new Date(((long)pbContest.getStartDate())*1000));
        }

        if (pbContest.getEndDate() > 0){
            updateContest.setEndDate(new Date(((long)pbContest.getEndDate()) * 1000));
        }

        if (pbContest.getVoteStartDate() > 0){
            updateContest.setVoteStartDate(new Date(((long)pbContest.getVoteStartDate()) * 1000));
        }

        if (pbContest.getVoteEndDate() > 0){
            updateContest.setVoteEndDate(new Date(((long)pbContest.getVoteEndDate()) * 1000));
        }

        updateContest.setIsAnonymous(pbContest.getIsAnounymous());
        updateContest.setConstantsOnly(pbContest.getContestantsOnly());

        if (pbContest.getGroup() != null && !StringUtil.isEmpty(pbContest.getGroup().getGroupId())){
            updateContest.setIsGroup(true);
            updateContest.setGroupId(pbContest.getGroup().getGroupId());
            updateContest.setJoinersType(pbContest.getJoinersType());

            // TODO setConstantsOnly
        }
        else{
            updateContest.setIsGroup(false);
        }

        updateContest.setDesc(pbContest.getDesc());
        updateContest.setStatementUrl(pbContest.getStatementUrl());

        updateContest.setCategoryType(pbContest.getCategory().getNumber());               // client set category
        updateContest.setLanguage(DBConstants.C_LANGUAGE_CHINESE);
        updateContest.setSummitCount(pbContest.getCanSubmitCount());                      // client set to 1

        updateContest.setIsAnonymous(pbContest.getIsAnounymous());
        updateContest.setMaxFlowerPerOpus(pbContest.getMaxFlowerPerOpus());           // set by client, default
        updateContest.setMaxFlowerPerContest(pbContest.getMaxFlowerPerContest());     // set by client, deafult

        List<Integer> awardRuleList = pbContest.getAwardRulesList();
        if (awardRuleList != null && awardRuleList.size() > 0){
            updateContest.setAwardRuleList(awardRuleList);
        }

        BasicDBObject updateObj = (BasicDBObject)updateContest.getDbObject();
        updateObj.removeField("_id");
        ContestManager.updateContest(pbContest.getContestId(), updateObj);
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    public static List<Contest> getGroupOngoingContestList(MongoDBClient mongoClient, String groupId, int category) {

        if (StringUtil.isEmpty(groupId)){
            return Collections.emptyList();
        }

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_CATEGORY, category);
        query.put(DBConstants.F_GROUPID, groupId);
        query.put(DBConstants.F_STATUS, GameConstantsProtos.PBContestStatus.Running_VALUE);
        DBObject returnFields = getContentReturnFields(false, false);
        return getContestList(mongoClient, query, returnFields, 0, 10000);
    }

    public static List<Contest> getAllGroupOngoingContestList(MongoDBClient mongoClient) {

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_IS_GROUP, true);
        query.put(DBConstants.F_STATUS, GameConstantsProtos.PBContestStatus.Running_VALUE);
        DBObject returnFields = getContentReturnFields(false, false);
        return getContestList(mongoClient, query, returnFields, 0, Integer.MAX_VALUE);
    }

    public static int getGroupOngoingContestCount(MongoDBClient mongoClient, String groupId, int category) {
        List<Contest> list = getGroupOngoingContestList(mongoClient, groupId, category);
        return list.size();
    }

    public static List<Contest> getFollowGroupContestList(MongoDBClient mongoClient, int offset, int limit, String userId, int category) {

        if (StringUtil.isEmpty(userId)){
            return Collections.emptyList();
        }

        List<ObjectId> list = FollowGroupManager.getInstance().getAllIdList(userId);
        if (list.size() == 0){
            return Collections.emptyList();
        }

        List<String> inValueList = new ArrayList<String>();
        for (ObjectId id : list){
            inValueList.add(id.toString());
        }

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_CATEGORY, category);
        query.put(DBConstants.F_GROUPID, new BasicDBObject("$in", inValueList));
        DBObject returnFields = getContentReturnFields(false, false);
        return getContestList(mongoClient, query, returnFields, offset, limit);
    }

    public static List<Contest> getOngoingGroupContestList(MongoDBClient mongoClient, int offset, int limit, int category, String sortField) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_CATEGORY, category);
        query.put(DBConstants.F_STATUS, GameConstantsProtos.PBContestStatus.Running_VALUE);
        query.put(DBConstants.F_IS_GROUP, true);
        DBObject returnFields = getContentReturnFields(false, false);
        return getContestList(mongoClient, query, returnFields, offset, limit, sortField);
    }
}
