package com.orange.game.api.service.bbs;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.service.CreateDataFileService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chaoso on 14-9-9.
 */
public class RecreateBBSBoardPostService extends CommonGameService {

    String boardId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        boardId = request.getParameter(ServiceConstant.PARA_BOARDID);
        return true;
    }

    @Override
    public void handleData() {
        CreateDataFileService createDataFileService = CreateDataFileService.getInstance();
        createDataFileService.recreateBBSBoardPostList(mongoClient,boardId);
    }
}
