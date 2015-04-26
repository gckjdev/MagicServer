package com.orange.game.api.service.message;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.GroupStatManager;
import com.orange.game.model.manager.group.GroupUserManager;
import net.sf.json.JSONObject;

import com.orange.common.upload.UploadManager.ParseResult;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Message;
import com.orange.game.model.manager.DrawGamePushManager;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.utils.ImageUploadManager;

public class SendMessageService extends CommonGameService {

	private static final int REPLY_AGREE = 0;
	private static final int REPLY_REJECT = 1;

	String appId;
	String from;
	String to;
	byte[] drawData;
	String text;

	int messageType;
	double longitude; // longitude of sending user
	double latitude; // latitude of sending user
	// boolean hasLocation = false;
	String reqMessageId;
	int replyResult = 0;

    boolean isGroup = false;
	
	ParseResult parseResult = null;

	@Override
	public String toString() {
		return "SendMessageService [appId=" + appId + ", from=" + from
				+ ", latitude=" + latitude + ", longitude=" + longitude
				+ ", messageType=" + messageType + ", replyResult="
				+ replyResult + ", reqMessageId=" + reqMessageId + ", text="
				+ text + ", to=" + to + ",group="+isGroup+"]";
	}

	@Override
	public void handleData() {
		
		// add black user check
		if (checkIsBlackByTargetUser(from, to)){
			return;
		}		
		
		String imageURL = null; 
		String thumbImageURL = null;
		
		if (parseResult != null){
			imageURL = parseResult.getLocalImageUrl();
			thumbImageURL = parseResult.getLocalThumbUrl();
		}

        if (isGroup){
            // check whether user can send message to this group
            String groupId = to;
            String userId = from;

            if (!GroupManager.isGroupMemberOrGuest(groupId, userId)){
                resultCode = ErrorCode.ERROR_GROUP_NOT_MEMBER;
                return;
            }

            GroupStatManager.didSendGroupMessage(to, text, drawData, messageType);
        }
		
		Message message = MessageManager.creatMessage(mongoClient, messageType,
				from, to, drawData, text, longitude, latitude, reqMessageId,
				replyResult, imageURL, thumbImageURL, isGroup, appId, true);




		if (message != null) {

//            if (!isGroup){
//                DrawGamePushManager.sendMessage(message, appId);
//            }

			JSONObject resultJsonObject = new JSONObject();
			resultJsonObject.put(ServiceConstant.PARA_MESSAGE_ID, message
					.getMessageId());
			resultJsonObject.put(ServiceConstant.PARA_CREATE_DATE, message
					.getCreateDateIntValue());
			
			if (parseResult != null){				
				resultJsonObject.put(ServiceConstant.PARA_IMAGE, parseResult.getImageUrl());								
				resultJsonObject.put(ServiceConstant.PARA_THUMB_IMAGE, parseResult.getThumbUrl());								
			}
			
			resultData = resultJsonObject;
		}
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		appId = request.getParameter(ServiceConstant.PARA_APPID);
		from = request.getParameter(ServiceConstant.PARA_USERID);
		to = request.getParameter(ServiceConstant.PARA_TO_USERID);
		text = request.getParameter(ServiceConstant.PARA_MESSAGETEXT);
		messageType = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, Message.MessageTypeText);

		if (!check(from, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;
		if (!check(to, ErrorCode.ERROR_PARAMETER_TOUSERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_TOUSERID_NULL))
			return false;

        isGroup = getBoolValueFromRequest(request, ServiceConstant.PARA_IS_GROUP, false);

		// set draw data for old version
		try {
			
			if (messageType == Message.MessageTypeImage){
				parseResult = ImageUploadManager.getMessageImageManager().uploadAndCreateThumbImageAndReturnRelativeURL(getRequest());
				if (parseResult == null){
					resultCode = ErrorCode.ERROR_UPLOAD_FILE;
					return false;
				}
				
				log.info("<sendMessage> image="+parseResult.getImageUrl());
			}
			else{			
				drawData = readPostData(request.getInputStream());
				if (drawData != null && messageType == Message.MessageTypeText) {
					messageType = Message.MessageTypeDraw;
				}
			}
		} catch (IOException e) {
			resultCode = ErrorCode.ERROR_GENERAL_EXCEPTION;
			return false;
		}
		switch (messageType) {
		case Message.MessageTypeLocationRequest:
			return checkAndSetLocationRequestParameter(request);

		case Message.MessageTypeLocationResponse:
			return checkAndSetLocationResponseParameter(request);

		default:
			break;
		}


		return true;
	}

	private boolean checkAndSetLocationResponseParameter(
			HttpServletRequest request) {
		
		replyResult = getIntValueFromRequest(request,
                ServiceConstant.PARA_REPLY_RESULT, REPLY_AGREE);

		reqMessageId = request.getParameter(ServiceConstant.PARA_REQUEST_MESSAGE_ID);
		if (StringUtil.isEmpty(reqMessageId)) {
			resultCode = ErrorCode.ERROR_PARAMETER_REPLY_MESSAGE_ID;
			return false;
		}
		
		if (replyResult == REPLY_REJECT){
			return true;
		}
		
		String latitudeString = request.getParameter(ServiceConstant.PARA_LATITUDE);
		String longitudeString = request.getParameter(ServiceConstant.PARA_LONGITUDE);
		if (StringUtil.isEmpty(latitudeString)) {
			resultCode = ErrorCode.ERROR_PARAMETER_NO_LOCATION;
			return false;
		}

		longitude = Double.parseDouble(longitudeString);
		latitude = Double.parseDouble(latitudeString);

		return true;
	}

	private boolean checkAndSetLocationRequestParameter(
			HttpServletRequest request) {
		String latitudeString = request
				.getParameter(ServiceConstant.PARA_LATITUDE);
		String longitudeString = request
				.getParameter(ServiceConstant.PARA_LONGITUDE);
		if (StringUtil.isEmpty(latitudeString)) {
			resultCode = ErrorCode.ERROR_PARAMETER_NO_LOCATION;
			return false;
		}
		longitude = Double.parseDouble(longitudeString);
		latitude = Double.parseDouble(latitudeString);
		return true;
	}

}
