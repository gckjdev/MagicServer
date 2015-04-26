package com.orange.game.api.service.wall;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.upload.UploadManager;
import com.orange.common.upload.UploadManager.ParseResult;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.wall.UserWall;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.wall.UserWallManager;
import com.orange.network.game.protocol.constants.GameConstantsProtos.PBWallType;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.DataQueryResponse;
import com.orange.network.game.protocol.model.DrawProtos.PBLayout;
import com.orange.network.game.protocol.model.DrawProtos.PBWall;

public class CreateUserWallService extends CommonGameService {

//	PBWall pbWall;
	String  userId;
//	String  backgroundImageUrl;
//	String  backgroundThumbImageUrl;
	UploadWallUtils.UploadWallResult uploadResult;
	
	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		byte[] data = null;
		
		userId = request.getParameter(ServiceConstant.PARA_USERID);

		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		uploadResult = UploadWallUtils.processUploadData(request);
		if (uploadResult.resultCode != ErrorCode.ERROR_SUCCESS){
			resultCode = uploadResult.resultCode;
			return false;
		}
				
		/*
		try {
			
			ParseResult result = UploadManager.getFormDataAndSaveImage(request,
							ServiceConstant.PARA_WALL_DATA,
							ServiceConstant.PARA_WALL_IMAGE, 
							UserWall.getFileUploadLocalDir(), 
							UserWall.getFileUploadRemoteDir());
			if (result != null) {
				data = result.getData();
				backgroundImageUrl = result.getLocalImageUrl();
				backgroundThumbImageUrl = result.getLocalThumbUrl();
				log.info("<CreateUserWallService> backgroundImageUrl="+backgroundImageUrl);

			} else {
				resultCode = ErrorCode.ERROR_PARAMETER_DRAWDATA_NULL;
				return false;
			}
						
			if (data == null){
				resultCode = ErrorCode.ERROR_POST_DATA_NULL;
				return false;
			}
			
			pbWall = PBWall.parseFrom(data);
			if (pbWall == null){
				resultCode = ErrorCode.ERROR_PROTOCOL_BUFFER_NULL;
				return false;
			}
			
						
		} catch (Exception e) {
			resultCode = ErrorCode.ERROR_GENERAL_EXCEPTION;
			return false;
		}
		*/
		
		return true;
	}

	@Override
	public void handleData() {		
		UserWall userWall = UserWallManager.createUserWall(mongoClient, uploadResult.pbWall, uploadResult.backgroundImageUrl);
		if (userWall == null){
			// failure
			byteData = CommonServiceUtils.protocolBufferErrorNoData(ErrorCode.ERROR_CREATE_WALL);
			return;
		}
		
		// set wall
		PBWall wall = PBWall.newBuilder().setWallId(userWall.getWallId()).
				setType(uploadResult.pbWall.getType()).
				setUserId(uploadResult.pbWall.getUserId()).
				setContent(PBLayout.newBuilder(uploadResult.pbWall.getContent()).setImageUrl(userWall.getBackgroundRemoteURL()).build()).				
				build();
		
		// set reponse
		DataQueryResponse.Builder builder = GameMessageProtos.DataQueryResponse
				.newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS);
		builder.setWall(wall);
		
		byteData = builder.build().toByteArray();		
	}

}
