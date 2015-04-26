package com.orange.game.api.service;

import com.mongodb.DBObject;
import com.orange.common.api.service.CommonService;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.App;
import com.orange.game.model.dao.app.AbstractApp;
import com.orange.game.model.dao.app.AppFactory;
import com.orange.game.model.manager.RelationManager;
import com.orange.game.model.manager.friend.FriendManager;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.DataQueryResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public abstract class CommonGameService extends CommonService {

	public AbstractApp app = null;
	public AbstractXiaoji xiaoji = null;
	
	protected String userId;
	protected String appId;
	protected String gameId;
    protected String version;
    protected String groupId;
	
	public boolean checkUserIdAndAppId(){
		if (!check(appId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY,
				ErrorCode.ERROR_PARAMETER_APPID_NULL))
			return false;
		
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;
		
		return true;
	}

    public boolean setAndCheckUserIdAndAppId(HttpServletRequest request) {
        setUserIdAndAppId(request);
        return checkUserIdAndAppId();
    }

    public boolean isVersionAbove70(){

        if (StringUtil.isEmpty(version)){
            return false;
        }

        if (StringUtil.isEmpty(appId)){
            return false;
        }

        Double versionValue = Double.parseDouble(version);
        if (appId.equalsIgnoreCase(DBConstants.APPID_LITTLEGEE)){
            if (versionValue >= 1.5){
                return true;
            }
            else{
                return false;
            }
        }
        else if (appId.equalsIgnoreCase(DBConstants.APPID_DRAW)){
            if (versionValue >= 7.0){
                return true;
            }
            else{
                return false;
            }
        }
        else if (appId.equalsIgnoreCase(DBConstants.APPID_SING)){
            return true;
//            if (versionValue >= 1.3){
//                return true;
//            }
//            else{
//                return false;
//            }
        }

        return true;
    }
	
	@Override
	protected boolean needSecurityCheck(){
		
		userId = getRequest().getParameter(ServiceConstant.PARA_USERID);
		appId = getRequest().getParameter(ServiceConstant.PARA_APPID);
		gameId = getRequest().getParameter(ServiceConstant.PARA_GAME_ID);
        groupId = getRequest().getParameter(ServiceConstant.PARA_GROUPID);
        version = getRequest().getParameter(ServiceConstant.PARA_VERSION);

        if (gameId != null){
            // use gameId to get xiaoji firstly
            xiaoji = XiaojiFactory.getInstance().getXiaojiByGameId(gameId);
        }

		if (appId != null){
			app = AppFactory.getInstance().getApp(appId);
			if (app != null && xiaoji == null){
                // if xiaoji is null then try get xiaoji by appId
				xiaoji = app.getXiaoji();
			}
			
			if (xiaoji == null){
				log.warn("Xiaoji object not found for appId="+appId);
			}
		}		



		// return false if this method doesn't need security check
		if (isSecureMethod)
			return true;
		
		return false;
	}
	
	public static byte[] protocolBufferWithErrorCode(int errorCode) {
		DataQueryResponse response = GameMessageProtos.DataQueryResponse
				.newBuilder().setResultCode(errorCode).build();
		return response.toByteArray();
	}

	
	@Override
	protected byte[] getPBDataByErrorCode(int errorCode){
		byte[] byteData = protocolBufferWithErrorCode(errorCode);
		return byteData;		
	}	
	
	protected boolean checkIsBlackByTargetUser(String userId, String targetUserId){
		
		if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(targetUserId))
			return false;

        if (FriendManager.getFriendblackmanager().hasRelation(targetUserId, userId)){
//		if (RelationManager.isUserBlackFriend(mongoClient, targetUserId, userId)){
			log.info("userId "+userId+" is black friend of "+targetUserId+", action is forbidden!");
			resultCode = ErrorCode.ERROR_USER_IS_BLACK_FRIEND; 
			return true;
		}
		
		return false;
	}

    protected void safePut(DBObject obj, String key, String value) {
        if (!StringUtil.isEmpty(value)) {
            obj.put(key, value);
        }
    }

    public void setUserIdAndAppId(HttpServletRequest request) {
        userId = request.getParameter(ServiceConstant.PARA_USERID);
        appId = request.getParameter(ServiceConstant.PARA_APPID);
        if (appId != null){
            gameId = App.getGameIdByAppId(appId);
        }
    }


}
