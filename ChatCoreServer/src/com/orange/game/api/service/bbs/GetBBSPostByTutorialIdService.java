package com.orange.game.api.service.bbs;

import com.orange.common.api.service.CommonParameter;
import com.orange.common.api.service.CommonService;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.bbs.BBSContent;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.dao.bbs.BBSUser;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.group.index.UserTopicIndexManager;
import com.orange.network.game.protocol.message.GameMessageProtos;
import org.bson.types.ObjectId;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaoso on 14-8-20.
 */
public class GetBBSPostByTutorialIdService extends CommonGameService {

    String tutorialId;
    String stageId;
    String stageName;
    String tutorialName;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        tutorialId = request.getParameter(ServiceConstant.PARA_TUTORIAL_ID);
        stageId = request.getParameter(ServiceConstant.PARA_STAGE_ID);
        stageName = request.getParameter(ServiceConstant.PARA_TUTORIAL_NAME);
        tutorialName = request.getParameter(ServiceConstant.PARA_STAGE_NAME);


        if(StringUtil.isEmpty(tutorialId)){
            return false;
        }
        if(StringUtil.isEmpty(stageId)){
            return false;
        }
        if(StringUtil.isEmpty(stageName)){
            stageName = "";
        }
        if(StringUtil.isEmpty(tutorialName)){
            tutorialName = "";
        }


        return true;
    }

    @Override
    public void handleData() {

        GameMessageProtos.DataQueryResponse response = null;
        BBSPost post = getTutorialStagePost();
        if(post == null){
            resultCode = ErrorCode.ERROR_BBS_POST_NOT_EXIST;
            byteData = CommonServiceUtils.protocolBufferNoData();
            return;
        }

        List<BBSPost> postList = new ArrayList<BBSPost>(1);
        postList.add(post);
        byteData = CommonServiceUtils.bbsPostListToProto(postList, null, null);
    }

    private BBSPost getTutorialStagePost() {
        String postId = BBSManager.hasStagePost(tutorialId, stageId);
        if (StringUtil.isEmpty(postId)) {

            log.info("<GetBBsPostByTutorialIdService> postId not found for stage, create new one");

            // create content
            String text = String.format(DBConstants.C_STAGE_POST_SUBJECT, stageName, tutorialName);
            BBSContent content = BBSManager.createContent(BBSContent.ContentTypeText, text,
                    null, null, null,
                    null, null,
                    null, 0);

            // set create user
            String createUserId = DBConstants.C_STAGE_POST_CREATE_USERID;
            User user = UserManager.findPublicUserInfoByUserId(createUserId);

            BBSUser createUser = BBSManager.getUser(createUserId,
                    user.getNickName(),
                    user.getAvatar(),
                    user.getGender());

            String boardId = DBConstants.C_STAGE_POST_BOARDID;
            BBSPost post = BBSManager.createPost(mongoClient, boardId,
                    DBConstants.APPID_DRAW, 0, createUser, content, 0, 0, false, true);

            UserTopicIndexManager.getManagerForMine().insertIndex(createUserId, post.getPostId());

            String resultPostId = post.getPostId();
            BBSManager.insertTutorialStagePost(tutorialId, stageId, resultPostId);
            return post;

        } else {
            log.info("<GetBBsPostByTutorialIdService> the postId==" + postId);
            return BBSManager.getBBSPostByPostId(mongoClient, postId, BBSManager.MODE_BBS);
        }
    }
}