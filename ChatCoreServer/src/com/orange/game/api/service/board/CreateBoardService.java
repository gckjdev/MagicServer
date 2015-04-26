package com.orange.game.api.service.board;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSBoard;
import com.orange.game.model.manager.bbs.BoardManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-2-19
 * Time: 下午2:26
 * To change this template use File | Settings | File Templates.
 */
public class CreateBoardService extends CommonGameService {

    String boardId;
    String boardName;
    int boardSeq;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        boardId = request.getParameter(ServiceConstant.PARA_BOARDID);
        boardName = request.getParameter(ServiceConstant.PARA_NAME);
        boardSeq = getIntValueFromRequest(request, ServiceConstant.PARA_SEQ, 0);

        return true;
    }

    @Override
    public void handleData() {

        BBSBoard board = BoardManager.createBoard(boardName, boardSeq);
        if (board == null){
            resultCode = ErrorCode.ERROR_CREATE_BOARD_FAIL;
        }
    }

}
