package com.orange.game.api.service.group.group;

import com.orange.common.upload.UploadManager;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.utils.ImageUploadManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-12-18
 * Time: 上午11:12
 * To change this template use File | Settings | File Templates.
 */
public class UpdateGroupIconService extends CommonGameService{
    private String groupId;
    private UploadManager.ParseResult parseResult;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);

        if (!check(groupId, ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY,
                ErrorCode.ERROR_PARAMETER_GROUPID_NULL)) {
            return false;
        }


        parseResult = ImageUploadManager.getGroupImageManager().uploadAndCreateThumbImageAndReturnRelativeURL(request);
        if (parseResult == null) {
            resultCode = ErrorCode.ERROR_UPLOAD_FILE;
            return false;
        }
        return true;

    }

    @Override
    public void handleData() {
        String imageURL = parseResult.getLocalImageUrl();
        resultCode = GroupManager.updateGroupMedalImage(mongoClient, userId, groupId, imageURL);
        imageURL = parseResult.getImageUrl();
        if (resultCode == 0){
            byteData = CommonServiceUtils.buildReponseWithURL(imageURL);
        } else{
            byteData = protocolBufferWithErrorCode(resultCode);
        }
    }
}
