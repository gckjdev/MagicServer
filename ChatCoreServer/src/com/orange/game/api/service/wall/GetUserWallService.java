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
import com.orange.network.game.protocol.model.DrawProtos.PBWall;

public class GetUserWallService extends CommonGameService {

	String wallId;
	
	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		wallId = request.getParameter(ServiceConstant.PARA_WALL_ID);

		if (!check(wallId, ErrorCode.ERROR_PARAMETER_WALLID_EMPTY,
				ErrorCode.ERROR_PARAMETER_WALLID_NULL))
			return false;
		
		return true;
	}

	@Override
	public void handleData() {
		UserWall wall = UserWallManager.getWallById(mongoClient, wallId);
		if (wall == null){
			byteData = CommonServiceUtils.protocolBufferErrorNoData(ErrorCode.ERROR_WALL_NOT_FOUND);			
			return;
		}
		
		// set wall
		PBWall pbWall = wall.toPBWall();
		log.info("<GetUserWallService> wall="+pbWall.toString());
		
		// set reponse
		DataQueryResponse.Builder builder = GameMessageProtos.DataQueryResponse
				.newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS);
		builder.setWall(pbWall);
		
		byteData = builder.build().toByteArray();		
		return;
	}

}
