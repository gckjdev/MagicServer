package com.orange.game.api.service.bbs;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.bbs.BBSManager;

public class DeleteBBSPostService extends CommonGameService {

    int deviceType; // required

	String postId;
	String boardId;
    int mode;

	@Override
	public void handleData() {
		BBSManager.deletePost(mongoClient, userId, postId, boardId, mode);
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
		deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);

		// post info
		postId = request.getParameter(ServiceConstant.PARA_POSTID);
		boardId = request.getParameter(ServiceConstant.PARA_BOARDID);
		
		if (!check(postId, ErrorCode.ERROR_PARAMETER_POSTID_EMPTY,
				ErrorCode.ERROR_PARAMETER_POSTID_NULL)) {
			return false;
		}

        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);

        if (BBSManager.isUserBlackInBoard(userId, boardId)){
            resultCode = ErrorCode.ERROR_USER_IS_BLACK_BOARD;
            return false;
        }


		return true;
	}

}
