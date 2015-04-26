package com.orange.game.api.service.bbs;

import com.orange.common.upload.UploadManager;
import com.orange.common.upload.UploadManager.ParseResult;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSContent;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.dao.bbs.BBSUser;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.GroupStatManager;
import com.orange.game.model.manager.group.index.UserTopicIndexManager;

import javax.servlet.http.HttpServletRequest;

public class CreateBBSPostService extends CommonGameService {

    int deviceType;
    // post content
    int contentType;
    String boardId;
    String text;
    byte[] drawData;
    String drawImageUrl;
    String drawThumbUrl;
    String imageUrl;
    String thumbUrl;
    // user info
    String nickName;
    String avatar;
    String gender;
    // reward
    int bonus;
    int mode;
    boolean isPrivate = false;

    String opusId;
    int opusCategory;

    @Override
    public void handleData() {


        //construct content
        BBSContent content = BBSManager.createContent(contentType, text,
                drawThumbUrl, drawImageUrl, drawData,
                thumbUrl, imageUrl,
                opusId, opusCategory);
        if (content == null) {
            resultCode = ErrorCode.ERROR_BBS_POST_TYPE;
            return;
        }

        //construct create user
        BBSUser createUser = BBSManager.getUser(userId, nickName, avatar, gender);

        //create post
        BBSPost post = BBSManager.createPost(mongoClient, boardId, appId, deviceType,
                createUser, content, bonus, mode, isPrivate);

        UserTopicIndexManager.getManagerForMine().insertIndex(userId, post.getPostId());

        //return new post infomation
        resultData = CommonServiceUtils.simplePostToJson(post);

        updatePopScore(post);

    }

    private void updatePopScore(BBSPost post) {
        if (xiaoji != null) {
            boolean hasImage = (imageUrl != null);
            double dataLength = (drawData != null) ? (drawData.length) : 0;
            xiaoji.popUserManager().createPost(userId, text, hasImage, dataLength);
        }
        if (mode == BBSManager.MODE_GROUP) {
            GroupStatManager.didCreatedTopic(boardId, post);
        }
    }

    @Override
    public String toString() {
        return "CreateBBSPostService [appId=" + appId + ", avatar=" + avatar
                + ", boardId=" + boardId + ", deviceType=" + deviceType
                + ", gender=" + gender + ", nickName=" + nickName + ", text="
                + text + ", type=" + contentType + ", userId=" + userId + "]";
    }

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {


        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        // required system parameters
        deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);

        // user information parameters
        nickName = request.getParameter(ServiceConstant.PARA_NICKNAME);
        gender = request.getParameter(ServiceConstant.PARA_GENDER);
        avatar = request.getParameter(ServiceConstant.PARA_AVATAR);

        // post info parameters
        boardId = request.getParameter(ServiceConstant.PARA_BOARDID);
        contentType = getIntValueFromRequest(request, ServiceConstant.PARA_CONTENT_TYPE, BBSContent.ContentTypeNo);
        text = request.getParameter(ServiceConstant.PARA_TEXT_CONTENT);

        opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);
        opusCategory = getIntValueFromRequest(request, ServiceConstant.PARA_CATEGORY, 0);

        if (!check(boardId, ErrorCode.ERROR_PARAMETER_BBSBOARDID_EMPTY,
                ErrorCode.ERROR_PARAMETER_BBSBOARDID_NULL)) {
            return false;
        }

        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);

        // reward
        bonus = getIntValueFromRequest(request, ServiceConstant.PARA_BONUS, 0);
        // parse image and data

        if (BBSManager.isUserBlackInBoard(userId, boardId)){
            resultCode = ErrorCode.ERROR_USER_IS_BLACK_BOARD;
            return false;
        }

        if (contentType == BBSContent.ContentTypeDraw) {
            ParseResult result = UploadManager.getFormDataAndSaveImage(request,
                    ServiceConstant.PARA_DRAW_DATA,
                    ServiceConstant.PARA_DRAW_IMAGE,
                    ServiceConstant.PARA_DRAW_BG_IMAGE,
                    BBSManager
                    .getDrawImageUploadLocalDir(), BBSManager
                    .getDrawImageUploadRemoteDir(),
                    true,
                    false,
                    true,
                    null,
                    null,
                    null);
            drawData = result.getData();
            drawImageUrl = result.getLocalImageUrl();
            drawThumbUrl = result.getLocalThumbUrl();

        } else if (contentType == BBSContent.ContentTypeImage ||
                contentType == BBSContent.ContentTypeOpusDraw ||
                contentType == BBSContent.ContentTypeOpusSing) {
            ParseResult result = UploadManager.getFormDataAndSaveImage(request,
                    null, ServiceConstant.PARA_IMAGE,
                    ServiceConstant.PARA_DRAW_BG_IMAGE,
                    BBSManager.getImageUploadLocalDir(),
                    BBSManager.getImageUploadRemoteDir(),
                    true,
                    false,
                    true,
                    null,
                    null,
                    null
            );

            imageUrl = result.getLocalImageUrl();
            thumbUrl = result.getLocalThumbUrl();
        }

        int privateValue = getIntValueFromRequest(request, ServiceConstant.PARA_ISPRIVATE, 0);
        isPrivate = (privateValue == 1);

        groupId = boardId;
        if (mode == BBSManager.MODE_GROUP && !GroupManager.isGroupMemberOrGuest(groupId, userId)){
            resultCode = ErrorCode.ERROR_GROUP_NOT_MEMBER;
            return false;
        }

        return true;
    }

}
