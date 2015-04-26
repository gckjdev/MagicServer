package com.orange.game.api.service.tutorial;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Message;
import com.orange.game.model.dao.tutorial.UserTutorial;
import com.orange.game.model.manager.tutorial.TutorialStatManager;
import com.orange.game.model.manager.tutorial.UserTutorialManager;
import com.orange.network.game.protocol.message.GameMessageProtos;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chaoso on 14-7-11.
 */
public class UserTutorialActionService extends CommonGameService {

    public static final int ACTION_UNKNOWN = 0;
    public static final int ACTION_ADD = 1;         // 开始学习/添加教程
    public static final int ACTION_DELETE = 2;      // 删除
    public static final int ACTION_PRACTICE = 3;    // 修炼
    public static final int ACTION_CONQUER = 4;     // 闯关
    public static final int ACTION_DOWNLOAD = 5;    // 下载教程所有关卡，或者单个关卡
    public static final int ACTION_GETALL = 6;      // 得到所有用户教程
    public static final int ACTION_UPDATE = 7;      // 简单更新所有用户教程
    public static final int DEVICE_UNKNOWN = 1;

    String tutorialId;                              // 教程ID，必填
    String remoteUserTutorialId;                    // 用户教程关系ID，action为DELETE/PRACTICE/PASS/DOWNLOAD需要
    String localUserTutorialId;                     // 客户端关系ID，可选，用于保存
    int action;

    String stageId;
    int stageIndex;

    // 用于闯关得分
    int score;
    String localOpusId;

    String deviceModel;
    String deviceOs;
    int deviceType;

    //用于分页
    private int offset;
    private int limit;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        tutorialId = request.getParameter(ServiceConstant.PARA_TUTORIAL_ID);
        remoteUserTutorialId = request.getParameter(ServiceConstant.PARA_REMOTE_USER_TUTORIAL_ID);
        localUserTutorialId = request.getParameter(ServiceConstant.PARA_LOCAL_USER_TUTORIAL_ID);
        action = getIntValueFromRequest(request, ServiceConstant.PARA_ACTION_TYPE, ACTION_UNKNOWN);

        stageId = request.getParameter(ServiceConstant.PARA_STAGE_ID);
        stageIndex = getIntValueFromRequest(request, ServiceConstant.PARA_STAGE_INDEX, 0);

        deviceModel = request.getParameter(ServiceConstant.PARA_DEVICEMODEL);
        deviceOs = request.getParameter(ServiceConstant.PARA_DEVICEMODEL);
        deviceType = getIntValueFromRequest(request,ServiceConstant.PARA_DEVICETYPE,DEVICE_UNKNOWN);

        offset = getIntValueFromRequest(request,ServiceConstant.PARA_OFFSET,0);
        limit = getIntValueFromRequest(request,ServiceConstant.PARA_LIMIT,1);

        return true;
    }

    @Override
    public void handleData() {

        UserTutorial retUserTutorial = null;
        GameMessageProtos.DataQueryResponse response = null;
        List<UserTutorial> retUserTutorialList = null;
        String statActionType = null;

        switch (action){
            case ACTION_ADD:
                retUserTutorial = addUserTutorial();
                if (retUserTutorial == null){
                    byteData = CommonServiceUtils.protocolBufferErrorNoData(ErrorCode.ERROR_USER_TUTORIAL_NULL);
                }
                else{
                    response = CommonServiceUtils.dateQueryResponseWithUserTutorial(retUserTutorial);
                    byteData = response.toByteArray();
                }

                statActionType = TutorialStatManager.ADD_TUTORIAL;
                break;

            case ACTION_DELETE:
                resultCode = deleteUserTutorial();
                byteData = CommonServiceUtils.protocolBufferErrorNoData(resultCode);
                break;

            case ACTION_PRACTICE:
                statActionType = TutorialStatManager.PRACTICE_DRAW;
                handleActionUpdate();
                break;

            case ACTION_CONQUER:
                statActionType = TutorialStatManager.CONQUER_DRAW;
                handleActionUpdate();
                break;

            case ACTION_UPDATE:
                handleActionUpdate();
                break;

            case ACTION_GETALL:
                retUserTutorialList = getUserTutorialList();
                if(retUserTutorialList == null){
                    byteData = CommonServiceUtils.protocolBufferErrorNoData(ErrorCode.ERROR_USER_TUTORIAL_NULL);
                }
                else{
                    byteData = CommonServiceUtils.userTutorialListToProtocolBuffer(retUserTutorialList);
                }
                break;
            default:
                resultCode = ErrorCode.ERROR_UNKNOWN_USER_TUTORIAL_ACTION;
                byteData = CommonServiceUtils.protocolBufferErrorNoData(resultCode);
                break;
        }

        TutorialStatManager.getInstance().insertTutorialAction(userId, tutorialId, stageId, 0, statActionType);
        log.info("<UserTutorialActionService> response=" + response+", resultCode="+resultCode);
    }

    private UserTutorial handleActionUpdate() {
        UserTutorial retUserTutorial = updateOrAddUserTutorial();
        GameMessageProtos.DataQueryResponse response = null;
        if (retUserTutorial == null){
            byteData = CommonServiceUtils.protocolBufferErrorNoData(ErrorCode.ERROR_USER_TUTORIAL_NULL);
        }
        else{
            response = CommonServiceUtils.dateQueryResponseWithUserTutorial(retUserTutorial);
            byteData = response.toByteArray();
        }
        return retUserTutorial;
    }

    private UserTutorial updateOrAddUserTutorial() {

        UserTutorial userTutorial = new UserTutorial();
        userTutorial.setUserId(userId);
        userTutorial.setTutorialId(tutorialId);
        userTutorial.setLocalUserTutorialId(localUserTutorialId);
        userTutorial.setRemoteUserTutorialId(remoteUserTutorialId);

        userTutorial.setStageIndex(stageIndex);
        userTutorial.setStageId(stageId);

        userTutorial.setDeleteStatus(0);

        userTutorial.setDeviceModel(deviceModel);
        userTutorial.setDeviceOs(deviceOs);
        userTutorial.setDeviceType(deviceType);

        userTutorial.setModifyDate(new Date());

        return UserTutorialManager.getInstance().updateOrAddUserTutorial(userTutorial);
    }

    //添加用户教程
    private UserTutorial addUserTutorial() {

        UserTutorial userTutorial = new UserTutorial();
        userTutorial.setUserId(userId);
        userTutorial.setTutorialId(tutorialId);
        userTutorial.setLocalUserTutorialId(localUserTutorialId);
        userTutorial.setDeleteStatus(0);

        userTutorial.setDeviceModel(deviceModel);
        userTutorial.setDeviceOs(deviceOs);
        userTutorial.setDeviceType(deviceType);

        userTutorial.setStageIndex(stageIndex);
        userTutorial.setStageId(stageId);

        userTutorial.setModifyDate(new Date());

        return UserTutorialManager.getInstance().addUserTutorial(userTutorial);
    }

    //删除用户教程
    private int deleteUserTutorial(){
        return UserTutorialManager.getInstance().deleteUserTutorial(this.userId,remoteUserTutorialId);
    }

    //取得所有用户教程列表
    private List<UserTutorial> getUserTutorialList(){
        return UserTutorialManager.getInstance().getUserTutorialList(this.userId,offset,limit);
    }
}



