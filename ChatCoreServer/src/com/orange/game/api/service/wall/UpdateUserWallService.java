package com.orange.game.api.service.wall;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.wall.UserWall;
import com.orange.game.model.manager.wall.UserWallManager;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.DataQueryResponse;
import com.orange.network.game.protocol.model.DrawProtos.PBLayout;
import com.orange.network.game.protocol.model.DrawProtos.PBWall;

public class UpdateUserWallService extends CommonGameService {

	String wallId;
	UploadWallUtils.UploadWallResult uploadResult;
	
	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		wallId = request.getParameter(ServiceConstant.PARA_WALL_ID);

		if (!check(wallId, ErrorCode.ERROR_PARAMETER_WALLID_EMPTY,
				ErrorCode.ERROR_PARAMETER_WALLID_NULL))
			return false;
		
		uploadResult = UploadWallUtils.processUploadData(request);
		if (uploadResult.resultCode != ErrorCode.ERROR_SUCCESS){
			resultCode = uploadResult.resultCode;
			return false;
		}
		
		return true;
	}

	@Override
	public void handleData() {
		UserWall wall = UserWallManager.updateUserWall(mongoClient, wallId, uploadResult.pbWall, uploadResult.backgroundImageUrl);
		if (wall == null){
			// failure
			byteData = CommonServiceUtils.protocolBufferErrorNoData(ErrorCode.ERROR_UPDATE_WALL);
			return;
		}
		
		// set wall
		PBWall retPBWall = PBWall.newBuilder().setWallId(wallId).
				setType(uploadResult.pbWall.getType()).
				setUserId(uploadResult.pbWall.getUserId()).
				setContent(PBLayout.newBuilder(uploadResult.pbWall.getContent()).setImageUrl(wall.getBackgroundRemoteURL()).build())
				.build();
		
		// set reponse
		DataQueryResponse.Builder builder = GameMessageProtos.DataQueryResponse
				.newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS);
		builder.setWall(retPBWall);
		
		byteData = builder.build().toByteArray();		
	}

}
