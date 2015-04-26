package com.orange.game.api.service.bbs;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.utils.StringUtil;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.manager.bbs.BBSManager;

public class EditBBSPostService extends CommonGameService {

	int deviceType;

	String postId;
	String boardId;

	int status;
    private int mode;

    @Override
	public void handleData() {

        if (!StringUtil.isEmpty(boardId) && mode == BBSManager.MODE_BBS){
            //Transfer
            BBSManager.transferBBSPost(mongoClient, userId, postId, boardId);
        }else{
            BBSManager.updatePostStatus(mongoClient, userId, postId, status, mode);
        }
//		BBSManager.updatePost(mongoClient, userId, postId, boardId, status, mode);
	}

	
	
	@Override
	public String toString() {
		return "EditBBSPostService [appId=" + appId + ", boardId=" + boardId
				+ ", deviceType=" + deviceType + ", postId=" + postId
				+ ", status=" + status + ", userId=" + userId + "]";
	}



	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);
		deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);
		postId = request.getParameter(ServiceConstant.PARA_POSTID);
		if (!check(postId, ErrorCode.ERROR_PARAMETER_POSTID_EMPTY,
				ErrorCode.ERROR_PARAMETER_POSTID_NULL)) {
			return false;
		}

		boardId = request.getParameter(ServiceConstant.PARA_BOARDID);
		status = getIntValueFromRequest(request,
                ServiceConstant.PARA_STATUS, BBSPost.StatusNormal);

        if (BBSManager.isUserBlackInBoard(userId, boardId)){
            resultCode = ErrorCode.ERROR_USER_IS_BLACK_BOARD;
            return false;
        }

		return true;
	}

}
