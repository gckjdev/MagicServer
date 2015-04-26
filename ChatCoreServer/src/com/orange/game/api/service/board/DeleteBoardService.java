package com.orange.game.api.service.board;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSBoard;
import com.orange.game.model.manager.bbs.BoardManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-2-19
 * Time: 下午2:27
 * To change this template use File | Settings | File Templates.
 */
public class DeleteBoardService extends CommonGameService {

    String boardId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        boardId = request.getParameter(ServiceConstant.PARA_BOARDID);
        return true;
    }

    @Override
    public void handleData() {

        BoardManager.deleteBoard(boardId);
    }
}
