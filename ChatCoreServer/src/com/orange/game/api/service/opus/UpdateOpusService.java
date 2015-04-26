package com.orange.game.api.service.opus;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.upload.UploadManager;
import com.orange.common.upload.UploadManager.ParseResult;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.service.DataService;

public class UpdateOpusService extends CommonGameService {

	String opusId;
	String userId;
	String appId;
	String drawImageUrl = null;
	String thumbImageUrl = null;
//	byte[] data = null;
	
	String type;
	String targetUserId;
	String description;
//	String targetUserNickName;
	
	boolean isZipData = false;
	int drawDataLen = 0;	
	String drawDataUrl;

	// add by Benson for non-compressed draw data, old data interface is true, new is false
	boolean isDataCompressed = true;


	
	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {

		log.info("request = " + request);

		appId = request.getParameter(ServiceConstant.PARA_APPID);
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);				
		
		type = request.getParameter(ServiceConstant.PARA_TYPE);
		targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);		
		description = request.getParameter(ServiceConstant.PARA_DESC);
		
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;
		if (!check(opusId, ErrorCode.ERROR_PARAMETER_OPUSID_EMPTY,
				ErrorCode.ERROR_PARAMETER_OPUSID_NULL))
			return false;
		
		String isZipDataString = request.getParameter(ServiceConstant.PARA_IS_DATA_ZIP);
		if (!StringUtil.isEmpty(isZipDataString)){
			isZipData = (Integer.parseInt(isZipDataString) != 0);
		}		
		
		String isDataCompressedString = request.getParameter(ServiceConstant.PARA_IS_DATA_COMPRESSED);
		if (!StringUtil.isEmpty(isDataCompressedString)){
			isDataCompressed = (Integer.parseInt(isDataCompressedString) != 0);
		}		

		ParseResult result = UploadManager.getFormDataAndSaveImage(request,
				ServiceConstant.PARA_DRAW_DATA,
				ServiceConstant.PARA_DRAW_IMAGE,
                ServiceConstant.PARA_DRAW_BG_IMAGE,
				OpusManager.getFileUploadLocalDir(),
				OpusManager.getFileUploadRemoteDir(),
				false,
				isZipData,
				isDataCompressed,
				DataService.getDrawFileUploadLocalDir(),
				DataService.getDrawFileUploadRemoteDir(),
                null
				);
		if (result != null) {			
			drawImageUrl = result.getLocalImageUrl();
			thumbImageUrl = result.getLocalThumbUrl();		
			drawDataUrl = result.getLocalZipFileUrl();
			
			if (isDataCompressed){
				DataService.getInstance().createUncompressDataFile(drawDataUrl);
			}
			else{
				DataService.getInstance().createCompressDataFile(drawDataUrl);
			}
			
		}

		return true;
	}

	@Override
	public void handleData() {
		// TODO change update data here
		OpusManager.updateDrawAction(mongoClient, opusId, drawImageUrl, thumbImageUrl, drawDataUrl, 
				drawDataLen, type, targetUserId,description);
	}

	@Override
	public String toString() {
		return "UpdateOpusService [opusId=" + opusId + ", userId=" + userId
				+ ", appId=" + appId + ", drawImageUrl=" + drawImageUrl
				+ ", thumbImageUrl=" + thumbImageUrl + "]";
	}

	
	
}
