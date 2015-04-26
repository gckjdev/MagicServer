package com.orange.game.model.service.contest;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.orange.common.scheduler.ScheduleService;
import com.orange.common.utils.PropertyUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.MongoGetIdListUtils;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.common.UserAward;
import com.orange.game.model.manager.*;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.manager.opus.contest.ContestTopOpusManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.XiaojiFactory;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-20
 * Time: 下午10:33
 * To change this template use File | Settings | File Templates.
 */
public class ContestService {
    private final static Logger log = Logger.getLogger(ContestService.class.getName());
    private static ContestService ourInstance = new ContestService();

    public static ContestService getInstance() {
        return ourInstance;
    }

    private ContestService() {



        if (PropertyUtil.getIntProperty("contest.gen_result", 0) == 0){
            log.info("contest.gen_result is 0, no need to generate contest info & result, return now");
            return;
        }
        else{
            log.info("contest.gen_result is 1, need to generate contest info & result");
        }

        ScheduleService.getInstance().scheduleEveryday(3, 0, 0, new Runnable(){

            @Override
            public void run() {
                // deduct group contest
                deductGroupContest();
            }

        }
        );

        List<Contest> list = ContestManager.getAllContestList(DBService.getInstance().getMongoDBClient(), 0, 0, DBConstants.C_LANGUAGE_CHINESE);
//        List<Contest> list = ContestManager.getTestContestList(DBService.getInstance().getMongoDBClient(), 0, 0, DBConstants.C_LANGUAGE_CHINESE);
        for (final Contest contest : list){

            if (contest.needGenerateResult()){
                if (contest.getVoteEndDate() == null){
                    log.info("schedule generate contest result for contest "+contest.getContestId()+" , "+contest.getTitle()+" but no end date, skip");
                    continue;
                }

                log.info("schedule generate contest result for contest "+contest.getContestId()+", "+contest.getTitle());
                Date date = new Date(contest.getVoteEndDate().getTime() + 60*5);
                ScheduleService.getInstance().scheduleAtDate(date, new Runnable(){
                    @Override
                    public void run() {
                        try{
                            Contest latestContest = ContestManager.getContestById(DBService.getInstance().getMongoDBClient(), contest.getContestId());
                            if (latestContest == null){
                                return;
                            }

                            if (latestContest.needGenerateResult() == false){
                                return;
                            }

                            if (latestContest.getIsGroup()){
                                generateGroupContestResult(latestContest);
                            }
                            else{
                                generateContestResult(latestContest);
                            }
                        }
                        catch (Exception e){
                            log.error("<generateContestResult> but catch exception = "+e.toString(), e);
                        }

                    }
                });
            }

            if (contest.isCompleteInsertUserOpus() == false){
                if (contest.getVoteEndDate() == null){
                    log.info("schedule insert user contest for contest "+contest.getContestId()+" , "+contest.getTitle()+" but no end date, skip");
                    continue;
                }

                if (contest.getIsAnonymous() == false){
                    // only for anonymous contest
                    log.info("contest "+contest.getContestId()+" is not anouymous, skip schedule insert user opus");
                    continue;
                }

                final Contest contestWithData = ContestManager.getContestByIdWithAllData(DBService.getInstance().getMongoDBClient(), contest.getContestId());

                log.info("schedule insert user contest for contest "+contestWithData.getContestId()+", "+contestWithData.getTitle());
                Date date = new Date(contest.getVoteEndDate().getTime() + 60*60*24);  // after 24 hours of vote end date
//                Date date = new Date(); // test
                ScheduleService.getInstance().scheduleAtDate(date, new Runnable(){
                    @Override
                    public void run() {
                        try{
                            insertUserContestOpus(contestWithData);
                        }
                        catch (Exception e){
                            log.error("<generateContestResult> but catch exception = "+e.toString(), e);
                        }

                    }
                });
            }

        }



    }

    private void insertUserContestOpus(Contest contest) {

        List<String> contestOpusIdList = contest.getOpusIdList();
        if (contestOpusIdList == null || contestOpusIdList.size() == 0){
            log.info("<insertUserContestOpus> but no opus, contestId="+contest.getContestId());
        }
        else{
            MongoGetIdListUtils idListUtils = new MongoGetIdListUtils<UserAction>();
            List<UserAction> opusList = idListUtils.getListByStringIdList(DBService.getInstance().getMongoDBClient(), DBConstants.T_OPUS, "_id", DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE, contestOpusIdList, OpusUtils.createReturnFields(), 0, 10000, UserAction.class);
            for (UserAction opus : opusList){
                XiaojiFactory.getInstance().getXiaoji(contest.getCategoryType()).userOpusManager().insertIndex(opus.getCreateUserId(), opus.getActionId());
            }
            log.info("<insertUserContestOpus> total "+contestOpusIdList.size()+" opus inserted, contestId="+contest.getContestId());
        }

        // update contest insert user opus status after completed
        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_INSERT_USER_OPUS_STATUS, 1);
        ContestManager.updateContest(contest.getContestId(), obj);

    }

    private void deductGroupContest() {

        List<Contest> list = ContestManager.getAllGroupOngoingContestList(DBService.getInstance().getMongoDBClient());
        for (Contest contest : list){
            deductContestAward(contest);
        }
    }

    private void deductContestAward(Contest contest) {

        if (!contest.getIsGroup()){
            return;
        }

        if (contest.hasDeductAwardForGroup()){
            log.info("<deductContestAward> already done! contestId=" + contest.getContestId());
            return;
        }

        List<Integer> awardRule = contest.getAwardRuleList();
        if (awardRule == null || awardRule.size() == 0){
            log.warn("<deductContestAward> but contest has no award rule! contestId=" + contest.getContestId());
            return;
        }

        // deduct money from group
        int sum = 0;
        for (Integer amount : awardRule){
            sum += amount.intValue();
        }
        GroupManager.systemDeductBalance(contest.getGroupId(), sum);

        BasicDBObject update = new BasicDBObject(DBConstants.F_IS_AWARD_DEDUCT, true);
        ContestManager.updateContest(contest.getContestId(), update);
    }

    public void generateGroupContestResult(Contest contest){

        String contestId = contest.getContestId();
        List<UserAction> topList = XiaojiFactory.getInstance().getDraw().contestTopOpusManager(contestId).getTopList(0, 20);

        List<Integer> awardRule = contest.getAwardRuleList();
        if (awardRule == null || awardRule.size() == 0){
            log.warn("<generateGroupContestResult> but contest has no award rule! contestId=" + contest.getContestId());
            return;
        }

        // deduct money from group
//        int sum = 0;
//        for (Integer amount : awardRule){
//            sum += amount.intValue();
//        }
//        GroupManager.systemDeductBalance(contest.getGroupId(), sum);

        deductContestAward(contest);

        // get winner result in contest
        int TOP_COUNT = Math.min(awardRule.size(), topList.size());
        BasicDBList winnerList = new BasicDBList();
        if (topList.size() >= TOP_COUNT){
            for (int i=0; i<TOP_COUNT; i++){
                UserAction opus = topList.get(i);
                int awardCoin = awardRule.get(i);
                UserAward userAward = userAwardFromOpus(opus, contest, DBConstants.DEFAULT_RANK_TYPE,
                        i+1, opus.getContestScore(), awardCoin);

                winnerList.add(userAward.getDbObject());
            }
            log.info("<generateContestResult> contest " + contest.getContestId() +" winner list="+winnerList.toString());
        }
        else{
            log.warn("<generateContestResult> contest " + contest.getContestId() + " but top list less than " + TOP_COUNT);
        }

        int MESSAGE_TOP = Math.min(awardRule.size(), topList.size());
        for (int i=0; i<MESSAGE_TOP; i++){
            UserAction opus = topList.get(i);
            int awardCoin = awardRule.get(i);

            UserManager.chargeAccount(DBService.getInstance().getMongoDBClient(), opus.getCreateUserId(),
                    awardCoin, DBConstants.C_CHARGE_SOURCE_CONTEST, null, null);

            int pos = i+1;

            String appId = "";
            String message = "";
            if (contest.isDraw()){
                appId = DBConstants.APPID_DRAW;
                message = "这位同学，恭喜你参加了【"+contest.getTitle()+"】画画大赛，在此我们隆重宣布，你获得第"+pos+"名"
                        +"，获得了"+ awardCoin+"金币，"+"绵薄金币何足道，生活有了画画，更加精彩，期待再接再励，绘出人生!";
            }
            else{
                appId = DBConstants.APPID_SING;
                message = "这位同学，恭喜你参加了【"+contest.getTitle()+"】声音大赛，在此我们隆重宣布，你获得第"+pos+"名"
                        +"，获得了"+ awardCoin+"金币，"+"绵薄金币何足道，生活有了大舌头，更加精彩，期待再接再励，唱出人生!";
            }

            MessageManager.sendSystemMessage(DBService.getInstance().getMongoDBClient(), opus.getCreateUserId(), message, DBConstants.APPID_DRAW, false);
        }


        // get special award in contest
        Map<Integer, String> ranks = contest.getAllRankType();
        BasicDBList awardList = new BasicDBList();
        if (ranks != null){
            Set<Integer> rankTypes = ranks.keySet();
            for (Integer rankType : rankTypes){
                if (rankType == DBConstants.DEFAULT_RANK_TYPE)
                    continue;

                ContestTopOpusManager manager = XiaojiFactory.getInstance().getDraw().contestSpecialTopOpusManager(contestId, rankType);
                if (manager == null){
                    continue;
                }

                // get the first one
                List<UserAction> list = manager.getTopList(0, 1);
                if (list.size() == 0){
                    log.info("<generateContestResult> contest " + contest.getContestId() +" but no top opus for rank "+rankType);
                    continue;
                }

                UserAction opus = list.get(0);
                int SPECIAL_AWARD_COIN = 0;
                UserAward userAward = userAwardFromOpus(opus, contest, rankType, 1, opus.getSpecialRankScore(rankType), SPECIAL_AWARD_COIN);
                awardList.add(userAward.getDbObject());

                String appId = "";
                String message = "";
                if (contest.isDraw()){
                    appId = DBConstants.APPID_DRAW;
                    message = "这位同学，恭喜你参加了【"+contest.getTitle()+"】画画大赛，在此我们隆重宣布，本届比赛的【"
                            +ranks.get(rankType)+"】的获得者就是你啦！"+"，请自行感谢CCAV, MTV, ATV吧！期待再接再励，更上一层楼！";
                }
                else{
                    appId = DBConstants.APPID_SING;
                    message = "这位同学，恭喜你参加了【"+contest.getTitle()+"】声音大赛，在此我们隆重宣布，本届比赛的【"
                            +ranks.get(rankType)+"】的获得者就是你啦！"+"，请自行感谢CCAV, MTV, ATV吧！期待再接再励，更上一层楼！";
                }

                MessageManager.sendSystemMessage(DBService.getInstance().getMongoDBClient(), opus.getCreateUserId(), message, appId, true);

            }

            log.info("<generateContestResult> contest " + contest.getContestId() +" award list="+awardList.toString());
        }
        else{
            log.info("<generateContestResult> contest " + contest.getContestId() +" no award list");
        }

        ContestManager.updateContestResult(contestId, winnerList, awardList);
    }

    public void generateContestResult(Contest contest){

        String contestId = contest.getContestId();
        List<UserAction> topList = XiaojiFactory.getInstance().getDraw().contestTopOpusManager(contestId).getTopList(0, 20);

        // get winner result in contest
        int TOP_COUNT = Math.min(5, topList.size());
        int TOP_AWARD_COIN[] = { 20000, 15000, 10000 };
        int TOP_AWARD_OTHER  = 5000;
        BasicDBList winnerList = new BasicDBList();
        if (topList.size() >= TOP_COUNT){
            for (int i=0; i<TOP_COUNT; i++){
                UserAction opus = topList.get(i);
                int awardCoin = TOP_AWARD_OTHER;
                if (i < TOP_AWARD_COIN.length){
                    awardCoin = TOP_AWARD_COIN[i];
                }

                UserAward userAward = userAwardFromOpus(opus, contest, DBConstants.DEFAULT_RANK_TYPE,
                        i+1, opus.getContestScore(), awardCoin);

                winnerList.add(userAward.getDbObject());
            }
            log.info("<generateContestResult> contest " + contest.getContestId() +" winner list="+winnerList.toString());
        }
        else{
            log.warn("<generateContestResult> contest " + contest.getContestId() + " but top list less than " + TOP_COUNT);
        }

        int MESSAGE_TOP = Math.min(20, topList.size());
        for (int i=0; i<MESSAGE_TOP; i++){
            UserAction opus = topList.get(i);
            int awardCoin = TOP_AWARD_OTHER;
            if (i < TOP_AWARD_COIN.length){
                awardCoin = TOP_AWARD_COIN[i];
            }

            UserManager.chargeAccount(DBService.getInstance().getMongoDBClient(), opus.getCreateUserId(),
                    awardCoin, DBConstants.C_CHARGE_SOURCE_CONTEST, null, null);

            int pos = i+1;

            String appId = "";
            String message = "";
            if (contest.isDraw()){
                appId = DBConstants.APPID_DRAW;
                message = "这位同学，恭喜你参加了【"+contest.getTitle()+"】画画大赛，在此我们隆重宣布，你获得第"+pos+"名"
                    +"，获得了"+ awardCoin+"金币，"+"绵薄金币何足道，生活有了画画，更加精彩，期待再接再励，绘出人生!";
            }
            else{
                appId = DBConstants.APPID_SING;
                message = "这位同学，恭喜你参加了【"+contest.getTitle()+"】声音大赛，在此我们隆重宣布，你获得第"+pos+"名"
                        +"，获得了"+ awardCoin+"金币，"+"绵薄金币何足道，生活有了大舌头，更加精彩，期待再接再励，唱出人生!";
            }

            MessageManager.sendSystemMessage(DBService.getInstance().getMongoDBClient(), opus.getCreateUserId(), message, DBConstants.APPID_DRAW, true);
        }


        // get special award in contest
        Map<Integer, String> ranks = contest.getAllRankType();
        BasicDBList awardList = new BasicDBList();
        if (ranks != null){
            Set<Integer> rankTypes = ranks.keySet();
            for (Integer rankType : rankTypes){
                if (rankType == DBConstants.DEFAULT_RANK_TYPE)
                    continue;

                ContestTopOpusManager manager = XiaojiFactory.getInstance().getDraw().contestSpecialTopOpusManager(contestId, rankType);
                if (manager == null){
                    continue;
                }

                // get the first one
                List<UserAction> list = manager.getTopList(0, 1);
                if (list.size() == 0){
                    log.info("<generateContestResult> contest " + contest.getContestId() +" but no top opus for rank "+rankType);
                    continue;
                }

                UserAction opus = list.get(0);
                int SPECIAL_AWARD_COIN = 0;
                UserAward userAward = userAwardFromOpus(opus, contest, rankType, 1, opus.getSpecialRankScore(rankType), SPECIAL_AWARD_COIN);
                awardList.add(userAward.getDbObject());

                String appId = "";
                String message = "";
                if (contest.isDraw()){
                    appId = DBConstants.APPID_DRAW;
                    message = "这位同学，恭喜你参加了【"+contest.getTitle()+"】画画大赛，在此我们隆重宣布，本届比赛的【"
                            +ranks.get(rankType)+"】的获得者就是你啦！"+"，请自行感谢CCAV, MTV, ATV吧！期待再接再励，更上一层楼！";
                }
                else{
                    appId = DBConstants.APPID_SING;
                    message = "这位同学，恭喜你参加了【"+contest.getTitle()+"】声音大赛，在此我们隆重宣布，本届比赛的【"
                            +ranks.get(rankType)+"】的获得者就是你啦！"+"，请自行感谢CCAV, MTV, ATV吧！期待再接再励，更上一层楼！";
                }

                MessageManager.sendSystemMessage(DBService.getInstance().getMongoDBClient(), opus.getCreateUserId(), message, appId, true);

            }

            log.info("<generateContestResult> contest " + contest.getContestId() +" award list="+awardList.toString());
        }
        else{
            log.info("<generateContestResult> contest " + contest.getContestId() +" no award list");
        }

        ContestManager.updateContestResult(contestId, winnerList, awardList);
    }

    private UserAward userAwardFromOpus(UserAction opus, Contest contest, int rankType, int rank, double score, int awardCoin) {
        UserAward userAward = new UserAward();
        userAward.setContestId(contest.getContestId());
        userAward.setCreateDate(new Date());
        userAward.setAwardName(contest.getRankTypeName(rankType));
        userAward.setAwardType(rankType);
        userAward.setUserId(opus.getCreateUserId());
        userAward.setOpusId(opus.getActionId());
        userAward.setRank(rank);
        userAward.setScore(score);
        userAward.setCoin(awardCoin);

        return userAward;
    }

    public void recaculateContestScore(){
        List<Contest> list = ContestManager.getAllContestListWithAllData(DBService.getInstance().getMongoDBClient(), 0, 0, DBConstants.C_LANGUAGE_CHINESE);
//        List<Contest> list = ContestManager.getTestContestList(DBService.getInstance().getMongoDBClient(), 0, 0, DBConstants.C_LANGUAGE_CHINESE);
        for (final Contest contest : list){

            List<String> contestOpusIdList = contest.getOpusIdList();
            if (contestOpusIdList == null || contestOpusIdList.size() == 0){
                continue;
            }

            MongoGetIdListUtils idListUtils = new MongoGetIdListUtils<UserAction>();
            List<UserAction> opusList = idListUtils.getListByStringIdList(DBService.getInstance().getMongoDBClient(), DBConstants.T_OPUS, "_id", DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE, contestOpusIdList, OpusUtils.createReturnFields(), 0, 10000, UserAction.class);
            for (UserAction opus : opusList){
                OpusManager.updateOpusScore(DBService.getInstance().getMongoDBClient(), opus.getActionId(), false, contest);
            }
        }

    }

    public static String getImageFileUploadLocalDir() {
        String para = String.format("upload.local.contestImage");
        String dir = System.getProperty(para);
        return (dir == null ? "" : dir);
    }

    public static String getImageFileUploadRemoteDir() {
        String para = String.format("upload.remote.contestImage");
        String dir = System.getProperty(para);
        return (dir == null ? "" : dir);
    }
}
