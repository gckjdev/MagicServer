package com.orange.game.model.dao;

import java.util.*;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.model.dao.common.IntKeyValue;
import com.orange.game.model.dao.common.UserAward;
import com.orange.game.model.manager.ScoreManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.index.GroupUserIndexManager;
import com.orange.game.model.manager.utils.ImageUploadManager;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.network.game.protocol.constants.GameConstantsProtos;
import com.orange.network.game.protocol.model.GroupProtos;
import org.bson.types.ObjectId;


import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;

public class Contest extends CommonData {

	//STATUS
	public static int CONTEST_STATUS_PENDING = 1;
	public static int CONTEST_STATUS_RUNNING = 2;
	public static int CONTEST_STATUS_PASSED = 3;

	//TYPE
	public static int CONTEST_TYPE_FREE = 1;
	public static int CONTEST_TYPE_TOPIC = 2;
	public static int CONTEST_TYPE_WORD = 3;
	public static int CONTEST_TYPE_WORD_LIST = 4;
    private boolean draw;

    public static final int JOINER_TYPE_ALL = 0;
    public static final int JOINER_TYPE_MEMBER = 1;
    public static final int JOINER_TYPE_MEMBER_GUEST = 2;

    public Contest() {
		super();
	}
	
	public Contest(DBObject object) {
		super(object);
	}

    public Contest(GroupProtos.PBContest pbContest, String imageUrl) {

        super();

        ObjectId contestId = new ObjectId();

        setContestId(contestId.toString());
        setCategoryType(pbContest.getCategory().getNumber());               // client set category
        setLanguage(DBConstants.C_LANGUAGE_CHINESE);
        setTitle(pbContest.getTitle());
        setContestUrl(imageUrl);
        setContestIPadUrl(imageUrl);
        setStatementUrl(pbContest.getStatementUrl());
        setSummitCount(pbContest.getCanSubmitCount());                      // client set to 1
        setStartDate(new Date(((long)pbContest.getStartDate())*1000));
        setEndDate(new Date(((long)pbContest.getEndDate()) * 1000));

        if (pbContest.getVoteStartDate() > 0){
            setVoteStartDate(new Date(((long)pbContest.getVoteStartDate()) * 1000));
        }

        if (pbContest.getVoteEndDate() > 0){
            setVoteEndDate(new Date(((long)pbContest.getVoteEndDate()) * 1000));
        }

        setDesc(pbContest.getDesc());
        setConstantsOnly(pbContest.getContestantsOnly());
        setIsAnonymous(pbContest.getIsAnounymous());
        setMaxFlowerPerOpus(pbContest.getMaxFlowerPerOpus());           // set by client, default
        setMaxFlowerPerContest(pbContest.getMaxFlowerPerContest());     // set by client, deafult

        // use default
        setFlowerRankWeight(3);
        setJudgeRankWeight(60);

        // set default rank types list
        BasicDBObject rankTypes = new BasicDBObject();
        rankTypes.put("1", "名次");

        setRankTypeList(rankTypes);
        setStatus(CONTEST_STATUS_RUNNING);

        if (pbContest.getGroup() != null && !StringUtil.isEmpty(pbContest.getGroup().getGroupId())){
            setIsGroup(true);
            setGroupId(pbContest.getGroup().getGroupId());
            setJoinersType(pbContest.getJoinersType());

            if (pbContest.getJoinersType() == JOINER_TYPE_ALL){
                setConstantsOnly(false);
            }
            else{
                setConstantsOnly(true);
            }
        }
        else{
            setIsGroup(false);
        }

        List<Integer> awardRuleList = pbContest.getAwardRulesList();
        if (awardRuleList != null && awardRuleList.size() > 0){
            setAwardRuleList(awardRuleList);
        }
    }

    public void setAwardRuleList(List<Integer> awardRuleList) {
        dbObject.put(DBConstants.F_AWARD_RULE_LIST, awardRuleList);

        if (awardRuleList != null){
            int total = 0;
            for (Integer value : awardRuleList){
                total += value.intValue();
            }

            dbObject.put(DBConstants.F_TOTAL_AWARD, total);
        }
    }

    public List<Integer> getAwardRuleList(){
        BasicDBList list = (BasicDBList)dbObject.get(DBConstants.F_AWARD_RULE_LIST);
        if (list == null){
            return Collections.emptyList();
        }

        List<Integer> retList = new ArrayList<Integer>();
        for (Object value : list){
            if (value instanceof Integer){
                retList.add((Integer)value);
            }
            else if (value instanceof Double){
                retList.add(((Double) value).intValue());
            }
            else if (value instanceof Long){
                retList.add(((Long) value).intValue());
            }
        }

        return retList;
    }

    public void setIsGroup(boolean value) {
        put(DBConstants.F_IS_GROUP, value);
    }

    public boolean getIsGroup(){
        return getBoolean(DBConstants.F_IS_GROUP);
    }

    public void setRankTypeList(BasicDBObject rankTypes) {
        put(DBConstants.F_RANK_TYPE_INFO, rankTypes);
    }

    public void setJoinersType(int joinersType) {

        put(DBConstants.F_JOINERS_TYPE, joinersType);
    }

    public void setGroupId(String groupId) {

        put(DBConstants.F_GROUPID, groupId);
    }

    public void setDesc(String desc) {

        put(DBConstants.F_DESC, desc);
    }

    public String getDesc(){
        return getString(DBConstants.F_DESC);
    }

    public void setStatus(int status) {

        put(DBConstants.F_STATUS, status);
    }

    public void setJudgeRankWeight(int i) {

        put(DBConstants.F_JUDGE_RANK_WEIGHT, i);
    }

    public void setFlowerRankWeight(int i) {

        put(DBConstants.F_FLOWER_RANK_WEIGHT, i);
    }

    public void setMaxFlowerPerContest(int maxFlowerPerContest) {

        put(DBConstants.F_MAX_FLOWER_PER_CONTEST, maxFlowerPerContest);
    }

    public void setMaxFlowerPerOpus(int maxFlowerPerOpus) {

        put(DBConstants.F_MAX_FLOWER_PER_OPUS, maxFlowerPerOpus);
    }

    public void setConstantsOnly(boolean value) {

        put(DBConstants.F_CONTESTANTS_ONLY, value);
    }

    public void setVoteEndDate(Date date) {

        put(DBConstants.F_VOTE_END_DATE, date);
    }

    public void setVoteStartDate(Date date) {

        put(DBConstants.F_VOTE_START_DATE, date);
    }

    //getter methods
	public Date getStartDate() {
		return getDate(DBConstants.F_START_DATE);
	}

	public Date getEndDate() {
		return getDate(DBConstants.F_END_DATE);
	}

	public int getType() {
		return getInt(DBConstants.F_TYPE);
	}

	public String getContestId() {
		return getObjectId().toString();
	}

	public String getTitle() {
		return getString(DBConstants.F_TITLE);
	}

	public String getVersion() {
		return getString(DBConstants.F_VERSION);
	}

	public List<String> getWordList() {
		return (List<String>) getObject(DBConstants.F_WORD_LIST);
	}

	public List<String> getParticipantList() {
		return (List<String>) getObject(DBConstants.F_PARTICIPANT_LIST);
	}

	public int getParticipantCount() {
		return getInt(DBConstants.F_PARTICIPANT_COUNT);
	}

	public int getOpusCount() {
		return getInt(DBConstants.F_OPUS_COUNT);
	}

	public boolean isHasParticipated() {
		return getBoolean(DBConstants.F_HAS_PARTICIPATED);
	}

	public ContestLimit getLimit() {
//		return limit;
		DBObject object =  (DBObject) getObject(DBConstants.F_LIMIT);
		if (object == null) {
			return null;
		}
		return new ContestLimit(object);
	}

	public String getContestUrl()
	{
		String url = getString(DBConstants.F_CONTEST_URL);
        return getRemoteURL(url);
    }
	
	public String getStatementUrl()
	{
		String url = getString(DBConstants.F_STATEMENT_URL);
        return getRemoteURL(url);
	}

    private String getRemoteURL(String path){
        if (StringUtil.isEmpty(path)){
            return path;
        }

        if (path.indexOf("http://") != -1){
            return path;
        }

        return AbstractXiaoji.getContestImageUploadManager().getRemoteURL(path);
    }
	
	public String getContestIPadUrl()
	{
		String url = getString(DBConstants.F_CONTEST_IPAD_URL);
        return getRemoteURL(url);
	}
	
	public String getStatementIPadUrl()
	{
        String url = getString(DBConstants.F_STATEMENT_IPAD_URL);
        return getRemoteURL(url);
	}

	//setter methods
	
	public void setContestUrl(String contestUrl)
	{
		put(DBConstants.F_CONTEST_URL, contestUrl);
	}
	
	public void setStatementUrl(String statementUrl)
	{
		put(DBConstants.F_STATEMENT_URL, statementUrl);
	}

	public void setContestIPadUrl(String contestUrl)
	{
		put(DBConstants.F_CONTEST_IPAD_URL, contestUrl);
	}
	
	public void setStatementIPadUrl(String statementUrl)
	{
		put(DBConstants.F_STATEMENT_IPAD_URL, statementUrl);
	}

	public void setStartDate(Date startDate) {
		put(DBConstants.F_START_DATE, startDate);
	}

	public void setEndDate(Date endDate) {
		put(DBConstants.F_END_DATE, endDate);
	}

	public void setType(int type) {
		put(DBConstants.F_TYPE, type);
	}

	public void setContestId(String contestId) {
		put(DBConstants.F_OBJECT_ID, new ObjectId(contestId));
	}

	public void setTitle(String topicDesc) {
		put(DBConstants.F_TITLE, topicDesc);
	}

	public void setVersion(String version) {
		put(DBConstants.F_VERSION, version);
	}

	public void setWordList(List<String> wordList) {
		put(DBConstants.F_WORD_LIST, wordList);
	}

	public void setParticipantList(List<String> participantList) {
		put(DBConstants.F_PARTICIPANT_LIST, participantList);
	}

	public void setParticipantCount(int participantCount) {
		put(DBConstants.F_PARTICIPANT_COUNT, participantCount);
	}

	public void setOpusCount(int opusCount) {
		put(DBConstants.F_OPUS_COUNT, opusCount);
	}

	public void setHasParticipated(boolean hasParticipated) {
		put(DBConstants.F_HAS_PARTICIPATED, hasParticipated);
	}

	public void setLimit(ContestLimit limit) {
		put(DBConstants.F_LIMIT, limit.getDbObject());
	}
	
	public void setLanguage(int language) {
		put(DBConstants.F_LANGUAGE, language);		
	}

    public boolean canThrowFlower() {

        Date now = new Date();
        if (now.after(getVoteStartDate()) && now.before(getVoteEndDate())){
            return true;
        }
        else{
            return false;
        }
    }

    public BasicDBList getRankTypeList() {
       return (BasicDBList)dbObject.get(DBConstants.F_RANK_TYPE_INFO);
    }

    public int getIntStartDate() {
        return getIntDate(DBConstants.F_START_DATE);
    }

    public int getIntEndDate() {
        return getIntDate(DBConstants.F_END_DATE);
    }

    public int getIntVoteStartDate() {
        int value = getIntDate(DBConstants.F_VOTE_START_DATE);
        if (value == 0){
            return getIntStartDate();
        }
        else{
            return value;
        }
    }

    public int getIntVoteEndDate() {
        int value = getIntDate(DBConstants.F_VOTE_END_DATE);
        if (value == 0){
            return getIntEndDate();
        }
        else{
            return value;
        }
    }

    public int getStatus() {
        return getInt(DBConstants.F_STATUS);
    }

    public Map<Integer, String> getAllRankType(){
        return getIntKeyValueObject(DBConstants.F_RANK_TYPE_INFO);
    }

    public IntKeyValue getRankTypeInfo(int awardType) {

//        Map<Integer, String> allRankTypeInfo = getAllRankType();
//        if (allRankTypeInfo == null || allRankTypeInfo.containsKey(awardType) == false)
//            return null;
//
//        String value = allRankTypeInfo.get(awardType);

        return new IntKeyValue(awardType, getRankTypeName(awardType));
    }

    public String getRankTypeName(int awardType) {

        Map<Integer, String> allRankTypeInfo = getAllRankType();
        if (allRankTypeInfo == null || allRankTypeInfo.containsKey(awardType) == false)
            return null;

        String value = allRankTypeInfo.get(awardType);
        return value;
    }

    public boolean needGenerateResult() {

//        if (getStatus() != CONTEST_STATUS_RUNNING){
//            log.info("contest "+getContestId()+","+getTitle()+" not running, no need to generate result");
//            return false;
//        }




        if (getWinnerList().size() == 0){
            return true;
        }
        else{
            log.info("contest "+getContestId()+","+getTitle()+" result has been generated, no need to generate result");
            return false;
        }
    }


    public boolean consestantsOnly(){
        return getBoolean(DBConstants.F_CONTESTANTS_ONLY);
    }

    public boolean canSubmit(String userId) {

        boolean result = canSubmit();
        if (result == false)
            return false;

        if (consestantsOnly()){
            if (getIsGroup()){
                switch (getJoinersType()){
                    case JOINER_TYPE_ALL:
                        return true;
                    case JOINER_TYPE_MEMBER_GUEST:
                        return GroupManager.isGroupMemberOrGuest(getGroupId(), userId);
                    case JOINER_TYPE_MEMBER:
                        return GroupUserIndexManager.getMemberInstance().isIdExistInList(getGroupId(), userId);
                }

                log.warn("<canSubmit> but group joiner type "+getJoinersType()+" invalid ");
                return false;
            }
            else{
                if (isUserInContestantList(userId)){
                    return true;
                }
                else{
                    return false;
                }
            }
        }
        else{
            return true;
        }
    }

    public boolean canSubmit() {

        Date start = getStartDate();
        Date end = getEndDate();

        if (start == null || end == null){
            return false;
        }

        Date now = new Date();

        if (start.before(now) && end.after(now)){
            return true;
        }

        return false;
    }

    public boolean canVote() {

        Date start = getVoteStartDate();
        Date end = getVoteEndDate();

        if (start == null || end == null){
            return false;
        }

        Date now = new Date();

        if (start.before(now) && end.after(now)){
            return true;
        }

        return false;
    }

    public double getFlowerRankWeight() {

        double value = getDouble(DBConstants.F_FLOWER_RANK_WEIGHT);
        if (value <= 0.0f)
            return ScoreManager.FLOWER_CONTEST_COEFFICIENT;
        else
            return value;
    }

    public double getJudgeRankWeight() {
        double value = getDouble(DBConstants.F_JUDGE_RANK_WEIGHT);
//        Double value = (Double)dbObject.get(DBConstants.F_JUDGE_RANK_WEIGHT);
        if (value <= 0.0f)
            return ScoreManager.JUDGE_CONTEST_COEFFICIENT;
        else
            return value;
    }

    public List<String> getOpusIdList() {
        return getStringList(DBConstants.F_OPUS_LIST);
    }



    public boolean isCompleteInsertUserOpus() {
        return ( getInt(DBConstants.F_INSERT_USER_OPUS_STATUS) == 1 );
    }

    public int getCategoryType() {
        return getInt(DBConstants.F_CATEGORY);
    }

    public void setCategoryType(int category) {
        put(DBConstants.F_CATEGORY, category);
    }

    public boolean isDraw() {
        return (getCategoryType() == GameConstantsProtos.PBOpusCategoryType.DRAW_CATEGORY_VALUE);
    }

    public String getGroupId() {
        return getString(DBConstants.F_GROUPID);
    }

    public int getJoinersType() {
        return getInt(DBConstants.F_JOINERS_TYPE);
    }

    public boolean hasDeductAwardForGroup() {
        return getBoolean(DBConstants.F_IS_AWARD_DEDUCT);
    }


    public static class ContestLimit extends CommonData
	{
		ContestLimit(){
			super();
		}
		public ContestLimit(DBObject object){
			super(object);
		}
		public int getLevel() {
			return getInt(DBConstants.F_LEVEL);
		}
		public int getOpusCount() {
			return getInt(DBConstants.F_OPUS_COUNT);
		}
		public void setLevel(int level) {
			put(DBConstants.F_LEVEL, level);
		}
		public void setOpusCount(int opusCount) {
			put(DBConstants.F_OPUS_COUNT, opusCount);
		}
	}


	public void setSummitCount(int count) {
		put(DBConstants.F_SUBMIT_COUNT, count);
	}
	public int getSummitCount() {
		return getInt(DBConstants.F_SUBMIT_COUNT);
	}

    public int getContestType(){
        return getInt(DBConstants.F_TYPE);
    }

    public void setContestType(int type){
        dbObject.put(DBConstants.F_TYPE, type);
    }

    public boolean getIsAnonymous(){
        return getBoolean(DBConstants.F_ANONYMOUS);
    }

    public void setIsAnonymous(boolean value){
        dbObject.put(DBConstants.F_ANONYMOUS, value);
    }

    public List<String> getJudgeList(){
        return getStringList(DBConstants.F_JUDGES);
    }

    public List<String> getReporterList(){
        return getStringList(DBConstants.F_REPORTERS);
    }

    public List<String> getContestantList(){
        return getStringList(DBConstants.F_CONTESTANTS);
    }

    public boolean isUserInContestantList(String userId){
        List<String> list = getContestantList();
        return (list.indexOf(userId) != -1);
    }

    public Date getVoteStartDate(){
        Date date = getDate(DBConstants.F_VOTE_START_DATE);
        if (date == null)
            return getStartDate();
        else
            return date;
    }

    public Date getVoteEndDate(){
        Date date = getDate(DBConstants.F_VOTE_END_DATE);
        if (date == null)
            return getEndDate();
        else
            return date;
    }

    public int getMaxFlowerPerContest(){
        int value = getInt(DBConstants.F_MAX_FLOWER_PER_CONTEST);
        if (value <= 0){
            log.warn("<getMaxFlowerPerContest> max flower per contest is "+value+" <= 0, set to default, contestId = "+getContestId());
            value = DBConstants.C_DEFAULT_MAX_FLOWER_PER_CONTEST;
        }

        return value;
    }

    public int getMaxFlowerPerOpus(){
        int value = getInt(DBConstants.F_MAX_FLOWER_PER_OPUS);
        if (value <= 0){
            log.warn("<getMaxFlowerPerContest> max flower per opus is "+value+" <= 0, set to default, contestId = "+getContestId());
            value = DBConstants.C_DEFAULT_MAX_FLOWER_PER_OPUS;
        }

        return value;
    }

//    public int getMaxFlowerPerContest(){
//        int value = getInt(DBConstants.F_MAX_FLOWER_PER_CONTEST);
//        if (value <= 0){
//            log.warn("<getMaxFlowerPerContest> max flower per opus is "+value+" <= 0, set to default, contestId = "+getContestId());
//            value = DBConstants.C_DEFAULT_MAX_FLOWER_PER_CONTEST;
//        }
//
//        return value;
//    }


    public List<UserAward> getWinnerList(){
        return getUserAwardList(DBConstants.F_WINNER_LIST);
    }

    public void setWinnerList(List<String> winnerList){
        // TODO
        // contain more infor in winner list
    }

    public List<UserAward> getAwardResult(){
        return getUserAwardList(DBConstants.F_AWARD_LIST);
    }

}
