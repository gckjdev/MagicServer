package com.orange.game.api.service.bbs;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.upload.UploadManager;
import com.orange.common.upload.UploadManager.ParseResult;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSAction;
import com.orange.game.model.dao.bbs.BBSActionSource;
import com.orange.game.model.dao.bbs.BBSContent;
import com.orange.game.model.dao.bbs.BBSUser;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.GroupStatManager;


public class CreateBBSActionService extends CommonGameService {

	// user info
	String nickName;
	String avatar;
	String gender;

	int deviceType; // required

	// source post
	String sourcePostId; // required
	String sourceActionId;
	String sourcePostUid;
	String sourceActionUid;
	String briefText;
	int sourceActionType;
	String sourceActionNickName;

	// action content
	int contentType; // required
	int actionType;

	// content
	String text;
	byte[] drawData;

	// created data.
	String drawImageUrl;
	String drawThumbUrl;

	String imageUrl;
	String thumbUrl;
    private int mode;

    private String boardId;
    String opusId;
    int opusCategory;


    @Override
	public void handleData() {		

		BBSContent content = BBSManager.createContent(contentType, text,
				drawThumbUrl, drawImageUrl, drawData, thumbUrl, imageUrl, opusId, opusCategory);

		BBSUser createUser =  BBSManager.getUser(userId, nickName, avatar, gender);

		BBSActionSource source = new BBSActionSource(sourcePostId,
				sourcePostUid, sourceActionId, sourceActionUid,
				sourceActionNickName, actionType, briefText);

		BBSAction action = BBSManager.createAction(mongoClient, appId,
				deviceType, createUser, actionType, content, source, mode);


		resultData = CommonServiceUtils.simpleActionToJson(action);
        updatePopScore(action);

    }

    private void updatePopScore(BBSAction action) {
        if (xiaoji != null){
            boolean hasImage = (imageUrl != null);
            double dataLength = (drawData != null) ? (drawData.length) : 0;
            xiaoji.popUserManager().commentPost(userId, text, hasImage, dataLength);
        }
        if (mode == BBSManager.MODE_GROUP){
            GroupStatManager.didCreatedAction(boardId, action);
        }

    }


    @Override
	public boolean setDataFromRequest(HttpServletRequest request) {

            if (!setAndCheckUserIdAndAppId(request)){
                return false;
            }

            // required system parameters
            deviceType = getIntValueFromRequest(request,
            ServiceConstant.PARA_DEVICETYPE, 0);

            // user information parameters

            nickName = request.getParameter(ServiceConstant.PARA_NICKNAME);
            gender = request.getParameter(ServiceConstant.PARA_GENDER);
            avatar = request.getParameter(ServiceConstant.PARA_AVATAR);

            boardId = request.getParameter(ServiceConstant.PARA_BOARDID);
            opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);
            opusCategory = getIntValueFromRequest(request, ServiceConstant.PARA_CATEGORY, 0);

            // source post&&action info parameters
            sourcePostId = request.getParameter(ServiceConstant.PARA_POSTID);
            sourceActionId = request.getParameter(ServiceConstant.PARA_ACTIONID);


            sourcePostUid = request.getParameter(ServiceConstant.PARA_POST_UID);
            sourceActionUid = request.getParameter(ServiceConstant.PARA_ACTION_UID);
            sourceActionNickName = request
				.getParameter(ServiceConstant.PARA_ACTION_NICKNAME);
            briefText = request.getParameter(ServiceConstant.PARA_BRIEF_TEXT);
            sourceActionType = getIntValueFromRequest(request,
            ServiceConstant.PARA_SOURCE_ACTION_TYPE, BBSAction.ActionTypeNO);

            mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);


            if (!check(sourcePostId, ErrorCode.ERROR_PARAMETER_POSTID_EMPTY,
                            ErrorCode.ERROR_PARAMETER_POSTID_NULL)) {
                    return false;
		}

		// action && content type
		contentType = getIntValueFromRequest(request,
                ServiceConstant.PARA_CONTENT_TYPE, BBSContent.ContentTypeNo);

		actionType = getIntValueFromRequest(request,
                ServiceConstant.PARA_ACTION_TYPE, BBSAction.ActionTypeSupport);

		// action content
		text = request.getParameter(ServiceConstant.PARA_TEXT_CONTENT);

		// parse image and data

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
					null, ServiceConstant.PARA_IMAGE, ServiceConstant.PARA_DRAW_BG_IMAGE, BBSManager
							.getImageUploadLocalDir(), BBSManager
							.getImageUploadRemoteDir(),
							true,
							false,
							true,
							null,
							null,
							null);

			imageUrl = result.getLocalImageUrl();
			thumbUrl = result.getLocalThumbUrl();
		}

//        if (mode == BBSManager.MODE_GROUP){
            if (boardId == null || boardId.length() == 0){
                boardId = BBSManager.getBoardIdByPostId(mongoClient, sourcePostId, mode);
            }
//        }
//
//        if (boardId == null || boardId.length() == 0){
//            boardId = BBSManager.getBoardIdByPostId(mongoClient, sourcePostId, mode);
//        }

        groupId = boardId;
        if (mode == BBSManager.MODE_GROUP && !GroupManager.isGroupMemberOrGuest(groupId, userId)){
            resultCode = ErrorCode.ERROR_GROUP_NOT_MEMBER;
            return false;
        }

        if (BBSManager.isUserBlackInBoard(userId, boardId)){
            resultCode = ErrorCode.ERROR_USER_IS_BLACK_BOARD;
            return false;
        }

        return true;
	}

	@Override
	public String toString() {
		return "CreateBBSActionService [userId=" + userId + ", nickName="
				+ nickName + ", avatar=" + avatar + ", gender=" + gender
				+ ", appId=" + appId + ", deviceType=" + deviceType
				+ ", sourcePostId=" + sourcePostId + ", sourceActionId="
				+ sourceActionId + ", sourcePostUid=" + sourcePostUid
				+ ", sourceActionUid=" + sourceActionUid + ", briefText="
				+ briefText + ", sourceActionType=" + sourceActionType
				+ ", sourceActionNickName=" + sourceActionNickName
				+ ", contentType=" + contentType + ", actionType=" + actionType
				+ ", text=" + text
				+ ", drawImageUrl=" + drawImageUrl + ", drawThumbUrl="
				+ drawThumbUrl + ", imageUrl=" + imageUrl + ", thumbUrl="
				+ thumbUrl + "]";
	}
	
	
}
