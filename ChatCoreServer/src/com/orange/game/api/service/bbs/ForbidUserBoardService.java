package com.orange.game.api.service.bbs;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSBoard;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.bbs.BBSManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-2-26
 * Time: 下午5:30
 * To change this template use File | Settings | File Templates.
 */
public class ForbidUserBoardService extends CommonGameService {


    private static final int FORBID_USER = 1;
    private static final int UNFORBID_USER = 2;

    String targetUserId;
    int type;
    String boardId;
    int days;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        boardId = request.getParameter(ServiceConstant.PARA_BOARDID);
        targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, 1);
        days = getIntValueFromRequest(request, ServiceConstant.PARA_DAY, 0);

        return true;
    }

    @Override
    public void handleData() {

        if (type == FORBID_USER){
            BBSManager.forbidUserBoard(targetUserId, boardId, days);

            String message = "";
            BBSBoard board = BBSManager.getBBSBoardById(mongoClient, boardId);
            if (board == null)
                return;

            if (days > 0){
                message = "你已经被禁止在【"+board.getName()+"】发言，封禁天数为"+days+"天，到期会自动解禁，请注意遵守社区规则";
            }
            else{
                message = "你已经被禁止在【"+board.getName()+"】发言，请注意遵守规则";
            }
            MessageManager.sendSystemMessage(mongoClient, targetUserId, message, appId, true);

        }
        else{
            BBSManager.unforbidUserBoard(targetUserId, boardId);

            String message = "";
            BBSBoard board = BBSManager.getBBSBoardById(mongoClient, boardId);
            if (board == null)
                return;

            message = "已经解除你在版块【"+board.getName()+"】发言封禁";
            MessageManager.sendSystemMessage(mongoClient, targetUserId, message, appId, true);

        }
    }
}
