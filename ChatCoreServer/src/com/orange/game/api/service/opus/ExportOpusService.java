package com.orange.game.api.service.opus;

import com.orange.common.utils.FileUtils;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.service.DBService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-11-11
 * Time: 上午11:59
 * To change this template use File | Settings | File Templates.
 */
public class ExportOpusService extends CommonGameService {

    private static final int MAX_OPUS = 10000;
    String targetUserId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
		targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
        return true;
    }

    @Override
    public void handleData() {

        if (xiaoji == null){
            resultCode = ErrorCode.ERROR_XIAOJI_NULL;
            return;
        }

        List<UserAction> opusList;
        opusList = xiaoji.userOpusManager().getListAndConstructIndex(targetUserId, 0, MAX_OPUS);
        if (opusList.size() == 0){
            return;
        }

        if (StringUtil.isEmpty(targetUserId)){
            resultCode = ErrorCode.ERROR_PARAMETER_TARGET_USERID_EMPTY;
            return;
        }
        String exportDir = "/data/user_"+xiaoji.getCategoryName().toLowerCase()+"_image/"+targetUserId;
        FileUtils.createDir(exportDir);
        for (UserAction opus : opusList){
            // copy opus image to image dir
            String path = opus.createOpusLocalImageUrl();
            if (path != null){
                FileUtils.copyFileToDir(path, exportDir);
            }
        }
    }
}
