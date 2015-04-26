package com.orange.game.api.service.opus;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import org.bson.types.ObjectId;

public class CreateOpusService extends CommonGameService {

	String uid;
	String gender;
	String avatar;
	String nickName;
	String appId;
	String word;
	int level;
	int language;
	int score;
	byte[] drawData;
	String drawDataUrl;
	String targetUserId;
	int drawDataLen = 0;
    ObjectId opusId = new ObjectId();

    int type = 0;                                       // 学习类型，未知/闯关/修炼
    String tutorialId = null;
    String stageId = null;
    int stageIndex = 0;
    int chapterIndex = 0;
    String remoteUserTutorialId = null;                    // 用户教程关系ID
    String localUserTutorialId = null;                     // 客户端关系ID，可选，用于保存
    String chapterOpusId = null;
    int stageScore = 0;


    @Override
	public void handleData() {
        Date now = new Date();
		OpusManager.createDrawAction(mongoClient, opusId, uid, nickName, avatar,
				gender, null, appId, word, level, language, drawDataUrl, drawDataLen, targetUserId,
				null, null, null, null, UserAction.DEVICE_TYPE_NO, null, score, null, now, 0, 0, 0, 0, null,
                null, 0, 0, null,
                type, tutorialId, stageId, stageIndex, chapterIndex, remoteUserTutorialId, localUserTutorialId, chapterOpusId, stageScore);
	}

	@Override
	public String toString() {
		return "CreateOpusService [appId=" + appId + ", avatar=" + avatar
				+ ", gender=" + gender + ", language=" + language + ", level="
				+ level + ", nickName=" + nickName + ", uid=" + uid + ", word="
				+ word + "]";
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
						
		appId = request.getParameter(ServiceConstant.PARA_APPID);
		uid = request.getParameter(ServiceConstant.PARA_USERID);
		nickName = request.getParameter(ServiceConstant.PARA_NICKNAME);
		gender = request.getParameter(ServiceConstant.PARA_GENDER);
		avatar = request.getParameter(ServiceConstant.PARA_AVATAR);
		word = request.getParameter(ServiceConstant.PARA_WORD);
		level = Integer.valueOf(request
				.getParameter(ServiceConstant.PARA_LEVEL));
		language = Integer.valueOf(request
				.getParameter(ServiceConstant.PARA_LANGUAGE));
		String scoreStr = request
				.getParameter(ServiceConstant.PARA_WORD_SCORE);
		
		if (!StringUtil.isEmpty(scoreStr)){
			score = Integer.valueOf(scoreStr);
		}
		else{
			score = DBConstants.C_DEFAULT_WORD_SCORE;
		}
		

		if (!check(uid, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		if (!check(word, ErrorCode.ERROR_PARAMETER_WORD_EMPTY,
				ErrorCode.ERROR_PARAMETER_WORD_NULL))
			return false;

		try {
			drawData = readPostData(request.getInputStream());
			if (drawData != null){
				drawDataLen = drawData.length;
			}
//			drawDataUrl = CreateOpusWithImageService.processDrawData(drawData, false);
		} catch (IOException e) {
			resultCode = ErrorCode.ERROR_GENERAL_EXCEPTION;
			return false;
		}
		if (drawData == null) {
			resultCode = ErrorCode.ERROR_PARAMETER_DRAWDATA_NULL;
			return false;
		}

		targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);

		return true;
	}

}
