package com.orange.game.api.service.opus;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.CommentInfo;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.GroupStatManager;
import com.orange.game.model.manager.opus.contest.AllContestOpusManager;
import com.orange.game.model.manager.opus.contest.ContestFlowerManager;
import com.orange.game.model.manager.stat.ShareStatManager;
import com.orange.game.model.manager.user.PopUserManager;
import com.orange.game.model.service.DBService;
import com.orange.network.game.protocol.constants.GameConstantsProtos;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;

public class ActionOnOpusService extends CommonGameService {

    String uid;
    String gender;
    String avatar;
    String nickName;
    String appId;
    String opusId;
    String contestId;
    int vip;

    // guess parameter
    boolean correct;
    int score;
    int actionType;
    int category;
    Set<String> guessWords;
    String opusCreatorUid;

    // comment parameter
    String comment;
    // comment info
    int commentType;
    String commentId;
    String commentUserId;
    String commentNickName;
    String commentSummary;

    // action save para
    String actionName;

    String targetUserId;
    int exp = 0;                                // target user increase/decrease exp
    int balance = 0;                            // target user increase/decrease balance

    // for recommend opus
    int language = 1;


    boolean decreaseCoins;

    private CommentInfo createCommentInfo() {
        if (commentType == UserAction.TYPE_UNKNOW || StringUtil.isEmpty(commentId)) {
            return null;
        }

        CommentInfo info = new CommentInfo();
        info.setActionId(commentId);
        info.setActionNickName(commentNickName);
        info.setActionSummary(commentSummary);
        info.setActionUserId(commentUserId);
        info.setComment(comment);
        info.setType(commentType);
        return info;
    }

    @Override
    public void handleData() {

        // add black user check
        if ((actionType == UserAction.TYPE_COMMENT || actionType == UserAction.TYPE_CONTEST_COMMENT)
                && checkIsBlackByTargetUser(uid, opusCreatorUid)) {
            return;
        }

        switch (actionType) {
            case UserAction.TYPE_GUESS: {
                ShareStatManager.getInstance().incStat(ShareStatManager.OPUS_GUESS);
                OpusManager.guessOpus(mongoClient, appId, opusCreatorUid, opusId,
                        uid, gender, avatar, nickName, correct, score, guessWords, category, vip);
            }
            break;
            case UserAction.TYPE_COMMENT: {

                // check whether user is comment on his/her own opus
                UserAction opus = OpusManager.getOpusSimpleInfoById(opusId);
                if (opus != null) {
                    contestId = opus.getContestId();
                }

            /*
            UserAction opus = OpusManager.getDrawById(mongoClient, userId, opusId);
            if (opus == null){
                log.info("<commentOpus> but opusId not found for "+opusId);
                resultCode = ErrorCode.ERROR_USER_ACTION_INVALID;
                return;
            }

            if (opus.isContestDraw()){
                Contest contest = null;
                contestId = opus.getContestId();
                if (!StringUtil.isEmpty(contestId)){
                    contest = ContestManager.getContestById(DBService.getInstance().getMongoDBClient(), contestId);
                    if (contest == null){
                        log.info("<commentOpus> but contest not found for "+contestId);
                        resultCode = ErrorCode.ERROR_CONTEST_NOT_FOUND;
                        return;
                    }
                }

                if (contest != null && contest.canVote() && contest.getIsAnonymous()){

                    if (opus.isContestDraw() && opus.getCreateUserId().equalsIgnoreCase(userId)){
                        log.info("<commentOpus> user "+userId+" cannot comment his/her own opus "+opusId);
                        resultCode = ErrorCode.ERROR_USER_COMMENT_OWN_OPUS_CONTEST;
                        return;
                    }
                }
            }
            */

                ShareStatManager.getInstance().incStat(ShareStatManager.OPUS_COMMENT);
                CommentInfo info = createCommentInfo();
                String actionId = OpusManager.commentOpus(mongoClient, appId,
                        opusCreatorUid, opusId, uid, gender, avatar, nickName,
                        comment, info, contestId, category, vip);
                resultData = CommonServiceUtils.actionIdToJson(actionId);

            }
            break;

            case UserAction.TYPE_CONTEST_COMMENT: {
                ShareStatManager.getInstance().incStat(ShareStatManager.OPUS_COMMENT);
                ShareStatManager.getInstance().incStat(ShareStatManager.OPUS_CONTEST_COMMENT);
                CommentInfo info = createCommentInfo();
                String actionId = OpusManager.contestCommentOpus(mongoClient, appId,
                        opusCreatorUid, opusId, uid, gender, avatar, nickName,
                        comment, info, contestId, category, vip);
                resultData = CommonServiceUtils.actionIdToJson(actionId);
            }
            break;

            case UserAction.TYPE_TOMATO:
                log.info("<ActionOnOpusService> throw tomato not allowed anymore.");
                resultCode = ErrorCode.ERROR_ACTION_NOT_SUPPORT;
                break;

            case UserAction.TYPE_FLOWER: {
                if (decreaseCoins) {
                    User user = UserManager.findPublicUserInfoByUserId(mongoClient, userId);
                    if (user != null) {
                        if (!user.isVip()) {
                            if (user.getBalance() > ServiceConstant.SEND_FLOWER_COST) {
                                UserManager.deductAccount(mongoClient, userId, ServiceConstant.SEND_FLOWER_COST, DBConstants.C_CHARGE_SOURCE_FLOWER);
                            } else {
                                resultCode = ErrorCode.ERROR_BALANCE_NOT_ENOUGH;
                                return;
                            }
                        }
                    } else {
                        resultCode = ErrorCode.ERROR_USER_NOT_FOUND;
                        return;
                    }
                }

                ShareStatManager.getInstance().incStat(ShareStatManager.OPUS_FLOWER);
                if (xiaoji != null) {
                    log.info("throw flower to target user " + targetUserId);
                    xiaoji.newStarUserManager().updateNewStarScore(targetUserId, 1, 0);
                }
                Contest contest = null;
                if (!StringUtil.isEmpty(contestId)) {

                    contest = ContestManager.getContestById(DBService.getInstance().getMongoDBClient(), contestId);
                    if (contest == null) {
                        log.info("<throwFlower> but contest not found for " + contestId);
                        resultCode = ErrorCode.ERROR_CONTEST_NOT_FOUND;
                        return;
                    }

                    if (contest.canThrowFlower() == false) {
                        log.info("<throwFlower> exceed contest vote date end, cannot throw flower");
                        resultCode = ErrorCode.ERROR_CONTEST_EXCEED_THROW_FLOWER_DATE;
                        return;
                    }

                    // contest, check contest's max flower per user
                    int flowerTimes = 0;
                    int maxFlowerTimes = contest.getMaxFlowerPerContest(); //ContestFlowerManager.getInstance().getMaxFlowerTimes(contestId);
                    flowerTimes = ContestFlowerManager.getInstance().incFlowerTimes(contestId, uid);
                    if (ContestFlowerManager.getInstance().isReachMaxFlower(contest, flowerTimes)) {
                        log.info("<throwFlower> flower times " + flowerTimes + " reach max flower");
                        resultCode = ErrorCode.ERROR_CONTEST_REACH_MAX_FLOWER;
                        return;
                    }

                    JSONObject obj = new JSONObject();
                    obj.put(ServiceConstant.PARA_FLOWER_TIMES, flowerTimes);
                    obj.put(ServiceConstant.PARA_MAX_FLOWER_TIMES, maxFlowerTimes);

                    resultData = obj;
                } else {
                    // not contest, need to check whether it's contest opus
                    if (AllContestOpusManager.getInstance().isContestOpus(opusId)) {
                        // it's contest opusId but didn't pass contestId, then this request is sent by OLD client
                        log.info("<throwFlower> opus is contest opus but CLIENT is old version, return failure");
                        resultCode = ErrorCode.ERROR_CONTEST_OLD_CLIENT_THROW_FLOWER;
                        return;
                    }
                }

                OpusManager.throwItemToOpus(mongoClient, appId, opusCreatorUid,
                        opusId, uid, gender, avatar, nickName, actionType, contestId, contest, category, vip);

                // update group fame
                GroupStatManager.didThrowFlower(mongoClient, opusCreatorUid);
            }
            break;

            case UserAction.TYPE_PLAY: {
                ShareStatManager.getInstance().incStat(ShareStatManager.OPUS_PLAY);
                OpusManager.actionPlayOpus(mongoClient, appId, opusId, uid, actionName, category);
            }
            break;


            case UserAction.TYPE_SAVE: {
                ShareStatManager.getInstance().incStat(ShareStatManager.OPUS_FAVORITE);
                OpusManager.actionSaveOpus(mongoClient, appId, opusId, uid, actionName, category);
            }
            break;

            case UserAction.TYPE_REMOVE_FAVORITE:
                OpusManager.removeUserOpusFavorite(mongoClient, appId, opusId, uid, actionName);
                break;

            case UserAction.TYPE_RECOMMEND: {
                xiaoji.featureOpusManager(language).featureOpus(uid, opusId);
            }
            break;

            case UserAction.TYPE_UNRECOMMEND: {
                xiaoji.featureOpusManager(language).unfeatureOpus(uid, opusId);
            }
            break;

            case UserAction.TYPE_REJECT_DRAW_TO_ME: {
                log.info("<rejectDrawToMe> userId=" + uid + ", opusId=" + opusId);
                xiaoji.drawToUserOpusManager().removeId(uid, opusId, false);
                break;
            }

            default:
                break;
        }

        if (targetUserId != null && (exp > 0 || balance > 0)) {
            UserManager.incBalanceAndExpForAward(mongoClient, targetUserId, appId, balance, exp);
        }

        updatePopScore();
    }

    private void updatePopScore() {
        if (xiaoji != null) {
            PopUserManager popUserManager = xiaoji.popUserManager();
            if (UserAction.TYPE_COMMENT == actionType) {
                popUserManager.commentOpus(uid, comment);
            } else if (UserAction.TYPE_FLOWER == actionType) {
                popUserManager.sendFlower(uid);
            } else if (UserAction.TYPE_GUESS == actionType) {
                popUserManager.guessOpus(uid);
            } else {

            }
        }
    }


    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {


        appId = request.getParameter(ServiceConstant.PARA_APPID);
        uid = request.getParameter(ServiceConstant.PARA_USERID);
        nickName = request.getParameter(ServiceConstant.PARA_NICKNAME);
        gender = request.getParameter(ServiceConstant.PARA_GENDER);
        avatar = request.getParameter(ServiceConstant.PARA_AVATAR);
        opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);
        opusCreatorUid = request
                .getParameter(ServiceConstant.PARA_OPUS_CREATOR);

        String words = request.getParameter(ServiceConstant.PARA_WORD_LIST);
        guessWords = CommonServiceUtils.parseTextList(words);
        actionType = getIntValueFromRequest(request,
                ServiceConstant.PARA_ACTION_TYPE, UserAction.TYPE_UNKNOW);
        actionName = request.getParameter(ServiceConstant.PARA_ACTION_NAME);

        vip = getIntValueFromRequest(request, ServiceConstant.PARA_VIP, 0);

        language = getIntValueFromRequest(request, ServiceConstant.PARA_LANGUAGE, 1);    // 1 is chinese
        contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);
        decreaseCoins = getBoolValueFromRequest(request, ServiceConstant.PARA_DECREASE_COINS, false);

        // add for sing
        category = getIntValueFromRequest(request, ServiceConstant.PARA_CATEGORY, GameConstantsProtos.PBOpusCategoryType.DRAW_CATEGORY_VALUE); //xiaoji.getCategoryType();

        if (!check(uid, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
                ErrorCode.ERROR_PARAMETER_USERID_NULL))
            return false;
        if (!check(opusId, ErrorCode.ERROR_PARAMETER_OPUSID_EMPTY,
                ErrorCode.ERROR_PARAMETER_OPUSID_NULL))
            return false;
        if (!check(opusCreatorUid,
                ErrorCode.ERROR_PARAMETER_OPUS_CREATOR_UID_EMPTY,
                ErrorCode.ERROR_PARAMETER_OPUS_CREATOR_UID_NULL))
            return false;

        switch (actionType) {
            case UserAction.TYPE_GUESS:
                // guess parameter
                String correctString = request
                        .getParameter(ServiceConstant.PARA_CORRECT);
                correct = !(StringUtil.isEmpty(correctString));
                score = getIntValueFromRequest(request,
                        ServiceConstant.PARA_WORD_SCORE, 0);
                break;

            case UserAction.TYPE_COMMENT:
            case UserAction.TYPE_CONTEST_COMMENT:

                // comment parameter
                comment = request
                        .getParameter(ServiceConstant.PARA_COMMENT_CONTENT);
                if (!check(comment, ErrorCode.ERROR_PARAMETER_COMMENT_EMPTY,
                        ErrorCode.ERROR_PARAMETER_COMMENT_NULL))
                    return false;
                // set comment info
                commentId = request.getParameter(ServiceConstant.PARA_COMMENT_ID);
                commentNickName = request
                        .getParameter(ServiceConstant.PARA_COMMENT_NICKNAME);
                commentUserId = request
                        .getParameter(ServiceConstant.PARA_COMMENT_USERID);
                commentSummary = request
                        .getParameter(ServiceConstant.PARA_COMMENT_SUMMARY);
                commentType = getIntValueFromRequest(request,
                        ServiceConstant.PARA_COMMENT_TYPE, UserAction.TYPE_UNKNOW);

                if (actionType == UserAction.TYPE_CONTEST_COMMENT && StringUtil.isEmpty(contestId)) {
                    resultCode = ErrorCode.ERROR_PARAMETER_CONTESTID_NULL;
                    return false;
                }
                break;

            case UserAction.TYPE_FLOWER:
            case UserAction.TYPE_TOMATO:
            case UserAction.TYPE_SAVE:
            case UserAction.TYPE_REMOVE_FAVORITE:
                break;

            default:
                if (actionType > UserAction.TYPE_LAST_VALID_ACTION) {
                    resultCode = ErrorCode.ERROR_USER_ACTION_INVALID;
                    return false;
                }
        }

        targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
        String expString = request.getParameter(ServiceConstant.PARA_EXP);
        String balanceString = request.getParameter(ServiceConstant.PARA_ACCOUNT_BALANCE);

        if (!StringUtil.isEmpty(expString)) {
            exp = Integer.parseInt(expString);
        }

        if (!StringUtil.isEmpty(balanceString)) {
            balance = Integer.parseInt(balanceString);
        }


        return true;
    }


}
