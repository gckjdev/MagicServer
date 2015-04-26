package com.orange.game.api.service.wall;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.orange.common.api.service.CommonService;
import com.orange.common.upload.UploadManager;
import com.orange.common.upload.UploadManager.ParseResult;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.wall.UserWall;
import com.orange.game.model.service.DataService;
import com.orange.network.game.protocol.model.DrawProtos.PBWall;

public class UploadWallUtils {

	public static final Logger log = Logger.getLogger(UploadWallUtils.class.getName());

	public static class UploadWallResult{
		int resultCode = ErrorCode.ERROR_SUCCESS;
		PBWall pbWall;
		String backgroundImageUrl;
	};
	
	public static UploadWallResult processUploadData(HttpServletRequest request){
		
		UploadWallResult wallResult = new UploadWallResult();		
		
		try {
			byte[] data;
			ParseResult result = UploadManager.getFormDataAndSaveImage(request,
							ServiceConstant.PARA_WALL_DATA,
							ServiceConstant.PARA_WALL_IMAGE,
                            ServiceConstant.PARA_DRAW_BG_IMAGE,
							UserWall.getFileUploadLocalDir(), 
							UserWall.getFileUploadRemoteDir(),
							true,
							false,
							false,
							null,
							null,
							null);
			if (result != null) {
				data = result.getData();
				wallResult.backgroundImageUrl = result.getLocalImageUrl();
//				backgroundThumbImageUrl = result.getLocalThumbUrl();
				log.info("<processUploadData> backgroundImageUrl="+wallResult.backgroundImageUrl);

			} else {
				wallResult.resultCode = ErrorCode.ERROR_PARAMETER_DRAWDATA_NULL;
				return wallResult;
			}
						
			if (data == null){
				wallResult.resultCode = ErrorCode.ERROR_POST_DATA_NULL;
				return wallResult;
			}
			
			wallResult.pbWall = PBWall.parseFrom(data);
			if (wallResult.pbWall == null){
				wallResult.resultCode = ErrorCode.ERROR_PROTOCOL_BUFFER_NULL;
				return wallResult;
			}
			
						
		} catch (Exception e) {
			wallResult.resultCode = ErrorCode.ERROR_GENERAL_EXCEPTION;
			return wallResult;
		}

		return wallResult;
	}
}
