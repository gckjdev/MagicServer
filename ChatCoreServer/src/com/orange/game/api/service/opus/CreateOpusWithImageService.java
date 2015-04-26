package com.orange.game.api.service.opus;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.model.dao.Contest;
import net.sf.json.JSONObject;

import com.orange.common.upload.UploadManager;
import com.orange.common.upload.UploadManager.ParseResult;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.service.DataService;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateOpusWithImageService extends CommonGameService {

	String uid;
	String gender;
	String avatar;
	String nickName;
	String signature;
	String appId;
	String word;
	int level;
	int language;
	byte[] drawData;
	String drawImageUrl;
	String targetUserId;
	String thumbImageUrl;

    String drawBgImageUrl;
    int bgImageWidth = 0;
    int bgImageHeight = 0;
    String bgImageName;

	String contestId;
    Contest contest;

	String deviceModel;
	int devicetype;
	String description;
	int score;
	
	// add by Benson for draw data save as zip file
	String drawDataUrl;
	boolean isZipData = false;
	int drawDataLen = 0;
	
	// add by Benson for non-compressed draw data, old data interface is true, new is false
	boolean isDataCompressed = true;

    // add 2014-5-24
    long strokes = 0;
    int spendTime = 0;
    Date draftCreateDate = null;
    Date draftCompleteDate = null;

    int canvasWidth = 0;
    int canvasHeight = 0;

    List<String> classList;
    ObjectId opusId;

    // 用于闯关作品提交的参数
    int type;                                       // 学习类型，未知/闯关/修炼
    String tutorialId;
    String stageId;
    int stageIndex;
    int chapterIndex;
    String remoteUserTutorialId;                    // 用户教程关系ID
    String localUserTutorialId;                     // 客户端关系ID，可选，用于保存
    String chapterOpusId;
    int stageScore;

    @Override
	public void handleData() {
		
		// add black user check
		if (checkIsBlackByTargetUser(uid, targetUserId)){
			return;
		}
		
		UserAction userAction = OpusManager.createDrawAction(mongoClient, opusId, uid,
				nickName, avatar, gender, signature, appId, word, level, language,
				drawDataUrl, drawDataLen, targetUserId, contestId, deviceModel, drawImageUrl,
				thumbImageUrl, devicetype, description, score,
                draftCreateDate, draftCompleteDate, strokes, spendTime,
                canvasWidth, canvasHeight,
                classList,
                drawBgImageUrl, bgImageWidth, bgImageHeight, bgImageName,
                type, tutorialId, stageId, stageIndex,
                chapterIndex, remoteUserTutorialId, localUserTutorialId,
                chapterOpusId, stageScore);

		JSONObject object = new JSONObject();
        if (userAction != null){
		    object.put(ServiceConstant.PARA_FEED_ID, userAction.getActionId());
            object.put(ServiceConstant.PARA_TOTAL_COUNT, userAction.getTotalCount());
            object.put(ServiceConstant.PARA_TOTAL_DEFEAT, userAction.getTotalDefeat());

            object.put(ServiceConstant.PARA_BEST_SCORE, userAction.getInt(DBConstants.F_BEST_SCORE));
            if (userAction.getString(DBConstants.F_BEST_OPUS_ID) != null){
                object.put(ServiceConstant.PARA_BEST_OPUS_ID, userAction.getString(DBConstants.F_BEST_OPUS_ID));
            }
            object.put(ServiceConstant.PARA_BEST_CREATE_DATE, userAction.getIntDate(DBConstants.F_BEST_CREATE_DATE));

            object.put(ServiceConstant.PARA_LATEST_SCORE, userAction.getInt(DBConstants.F_LATEST_SCORE));
            if (userAction.getString(DBConstants.F_LATEST_OPUS_ID) != null){
                object.put(ServiceConstant.PARA_LATEST_OPUS_ID, userAction.getString(DBConstants.F_LATEST_OPUS_ID));
            }
            object.put(ServiceConstant.PARA_LATEST_CREATE_DATE, userAction.getIntDate(DBConstants.F_LATEST_CREATE_DATE));
        }

		resultData = object;

        updatePopScore();
    }

    private void updatePopScore() {
        if (xiaoji != null){
            double dataLength = drawDataLen;
            xiaoji.popUserManager().createOpus(uid, dataLength);
        }
    }

    @Override
	public String toString() {
		return "CreateOpusWithImageService [appId=" + appId + ", avatar="
				+ avatar + ", drawImageUrl=" + drawImageUrl + ", gender="
				+ gender + ", language=" + language + ", level=" + level
				+ ", nickName=" + nickName + ", targetUserId=" + targetUserId
				+ ", uid=" + uid + ", word=" + word + ", contestId="
				+ contestId + "]";
	}

	/*
	public static String processDrawData(byte[] drawData, boolean isZipData){
		String drawDataUrl = null;
		if (drawData != null){
			if (isZipData){
				drawDataUrl = DataService.getInstance().createZipDataFile(drawData);
			}
			else{
				drawDataUrl = DataService.getInstance().createDataFile(drawData);
			}
		}		
		
		return drawDataUrl;
	}
	*/
	
	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {

		log.info("request = " + request);

		appId = request.getParameter(ServiceConstant.PARA_APPID);
		uid = request.getParameter(ServiceConstant.PARA_USERID);
		nickName = request.getParameter(ServiceConstant.PARA_NICKNAME);
		signature = request.getParameter(ServiceConstant.PARA_SIGNATURE);
		gender = request.getParameter(ServiceConstant.PARA_GENDER);
		avatar = request.getParameter(ServiceConstant.PARA_AVATAR);
		word = request.getParameter(ServiceConstant.PARA_WORD);
		level = Integer.valueOf(request
				.getParameter(ServiceConstant.PARA_LEVEL));
		language = Integer.valueOf(request
				.getParameter(ServiceConstant.PARA_LANGUAGE));
		targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
		contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);

		String isZipDataString = request.getParameter(ServiceConstant.PARA_IS_DATA_ZIP);
		if (!StringUtil.isEmpty(isZipDataString)){
			isZipData = (Integer.parseInt(isZipDataString) != 0);
		}
		
		String isDataCompressedString = request.getParameter(ServiceConstant.PARA_IS_DATA_COMPRESSED);
		if (!StringUtil.isEmpty(isDataCompressedString)){
			isDataCompressed = (Integer.parseInt(isDataCompressedString) != 0);
		}

		String scoreStr = request
				.getParameter(ServiceConstant.PARA_WORD_SCORE);
		
		if (!StringUtil.isEmpty(scoreStr)){
			score = Integer.valueOf(scoreStr);
		}
		else{
			score = DBConstants.C_DEFAULT_WORD_SCORE;
		}
		
		if (!StringUtil.isEmpty(contestId)) {
            contest = ContestManager.getContestById(mongoClient, contestId);
            if (contest != null){
                if (!contest.canSubmit(userId)){
                    resultCode = ErrorCode.ERROR_CONTEST_EXCEED_SUBMIT_OPUS;
                    return false;
                }
            }
		}

        Date now = new Date();
        draftCompleteDate = getDateValueFromRequest(request, ServiceConstant.PARA_COMPLETE_DATE, now);
        draftCreateDate = getDateValueFromRequest(request, ServiceConstant.PARA_DRAFT_CREATE_DATE, null);
        spendTime = getIntValueFromRequest(request, ServiceConstant.PARA_SPEND_TIME, 0);
        strokes = getIntValueFromRequest(request, ServiceConstant.PARA_STROKES, 0);

        canvasHeight = getIntValueFromRequest(request, ServiceConstant.PARA_HEIGHT, 0);
        canvasWidth = getIntValueFromRequest(request, ServiceConstant.PARA_WIDTH, 0);

		deviceModel = request.getParameter(ServiceConstant.PARA_DEVICEMODEL);

		devicetype = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, UserAction.DEVICE_TYPE_NO);

		description = request.getParameter(ServiceConstant.PARA_DESC);
		
		if (!check(uid, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		if (!check(word, ErrorCode.ERROR_PARAMETER_WORD_EMPTY,
				ErrorCode.ERROR_PARAMETER_WORD_NULL))
			return false;

        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, 0);
        tutorialId = request.getParameter(ServiceConstant.PARA_TUTORIAL_ID);
        stageId = request.getParameter(ServiceConstant.PARA_STAGE_ID);
        stageIndex = getIntValueFromRequest(request, ServiceConstant.PARA_STAGE_INDEX, 0);
        chapterIndex = getIntValueFromRequest(request, ServiceConstant.PARA_CHAPTER_INDEX, 0);
        remoteUserTutorialId = request.getParameter(ServiceConstant.PARA_REMOTE_USER_TUTORIAL_ID);
        localUserTutorialId = request.getParameter(ServiceConstant.PARA_LOCAL_USER_TUTORIAL_ID);
        chapterOpusId = request.getParameter(ServiceConstant.PARA_CHAPTER_OPUS_ID);
        stageScore = getIntValueFromRequest(request, ServiceConstant.PARA_STAGE_SCORE, 0);

        opusId = new ObjectId();
		ParseResult result = UploadManager.getFormDataAndSaveImage(request,
				ServiceConstant.PARA_DRAW_DATA,
				ServiceConstant.PARA_DRAW_IMAGE,
                ServiceConstant.PARA_DRAW_BG_IMAGE,
				OpusManager.getFileUploadLocalDir(), 
				OpusManager.getFileUploadRemoteDir(),
				false,
				isZipData,
				isDataCompressed,
				DataService.getDrawFileUploadLocalDir(),
				DataService.getDrawFileUploadRemoteDir(),
                opusId.toString()
				);
		if (result != null) {
			drawData = result.getData();
			drawDataLen = result.getDataLen();

			// save draw data as file
			drawDataUrl = result.getLocalZipFileUrl();
			
			drawImageUrl = result.getLocalImageUrl();
			thumbImageUrl = result.getLocalThumbUrl();

            drawBgImageUrl = result.getLocalBgImageUrl();
            bgImageHeight = result.getBgImageHeight();
            bgImageWidth = result.getBgImageWidth();
            bgImageName = request.getParameter(ServiceConstant.PARA_DRAW_BG_IMAGE);
			
			if (isDataCompressed){
				DataService.getInstance().createUncompressDataFile(drawDataUrl);
			}
			else{
				DataService.getInstance().createCompressDataFile(drawDataUrl);
			}
			
			if (drawDataUrl == null){
				resultCode = ErrorCode.ERROR_PARAMETER_DRAWDATA_NULL;
				return false;				
			}

            if (canvasWidth == 0 && canvasHeight == 0){
                canvasWidth = result.getImageWidth();
                canvasHeight = result.getImageHeight();
            }



		} else {
			resultCode = ErrorCode.ERROR_PARAMETER_DRAWDATA_NULL;
			return false;
		}

        String classListString = request.getParameter(ServiceConstant.PARA_CLASS);
        if (!StringUtil.isEmpty(classListString)){
            String[] classListStrings = classListString.split("\\" + ServiceConstant.DEFAULT_SEPERATOR);
            if (classListStrings == null || classListStrings.length == 0){
                resultCode = ErrorCode.ERROR_PARAMETER_CLASS_EMPTY;
                return false;
            }

            classList = new ArrayList<String>();
            for (int i = 0; i < classListStrings.length; i++) {
                if (!StringUtil.isEmpty(classListStrings[i])) {
                    classList.add(classListStrings[i]);
                }
            }
        }

        return true;
	}

}
