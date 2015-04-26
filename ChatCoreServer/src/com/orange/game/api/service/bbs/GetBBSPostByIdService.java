package com.orange.game.api.service.bbs;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSContent;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.group.GroupManager;

public class GetBBSPostByIdService extends CommonGameService {

	int deviceType; // required

	String postId;
    private int mode;


    @Override
	public void handleData() {
		BBSPost post = BBSManager.getBBSPostByPostId(mongoClient, postId, mode);
        if (post != null) {
            groupId = post.getBoardId();
//            if (mode == BBSManager.MODE_GROUP && post.isPrivate() && !GroupManager.isGroupMember(groupId, userId) ){
//                log.info("user is not member and psot is private, force set content");
//                post.setContent(new BBSContent("私密帖子"));
//            }

            String userGroupId = null;
            if (mode == BBSManager.MODE_GROUP){
                userGroupId = GroupManager.getStringGroupIdByUserId(mongoClient, userId);
            }

			List<BBSPost> postList = new ArrayList<BBSPost>(1);
			postList.add(post);
			byteData = CommonServiceUtils.bbsPostListToProto(postList, userId, userGroupId);
		}else{
			resultCode = ErrorCode.ERROR_BBS_POST_NOT_EXIST;
			byteData = CommonServiceUtils.protocolBufferErrorNoData(resultCode);
		}
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);

        deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);

		// user information parameters


		// post info
		postId = request.getParameter(ServiceConstant.PARA_POSTID);
		if (!check(postId, ErrorCode.ERROR_PARAMETER_POSTID_EMPTY,
				ErrorCode.ERROR_PARAMETER_POSTID_NULL)) {
			return false;
		}

        return true;
	}

}
