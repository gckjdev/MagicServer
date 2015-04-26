package com.orange.game.api.service.opus;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.model.dao.Contest;
import com.orange.game.model.dao.opus.Opus;
import net.sf.json.JSONObject;

import com.google.protobuf.InvalidProtocolBufferException;
import com.orange.common.upload.UploadManager;
import com.orange.common.upload.UploadManager.ParseResult;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.app.AppFactory;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.utils.ImageUploadManager;
import com.orange.game.model.service.DataService;
import com.orange.game.model.service.opus.OpusService;
import com.orange.network.game.protocol.model.OpusProtos.PBOpus;
import com.orange.network.game.protocol.model.OpusProtos.PBOpusOrBuilder;

public class NewCreateOpusService extends CommonGameService {

//	int language;
	String uploadDataType = "dat";		// e.g. zip, dat, or wav, mp3, etc
//	String drawDataUrl;
//	int drawDataLen = 0;	
	
	final static boolean isZipData = false;
	final static boolean isDataCompressed = false;
	
	String dataUrl;
	int     dataLen;
	String imageUrl;
	String thumbImageUrl;	
	
	PBOpus pbOpus;

	@Override
	public void handleData() {
		
		Opus opus = OpusService.getInstance().createOpus(xiaoji, userId, appId, gameId, pbOpus, dataUrl, dataLen, imageUrl, thumbImageUrl);
		if (opus == null){
			resultCode = ErrorCode.ERROR_CREATE_OPUS;
			return;
		}
		else{
			byteData = CommonServiceUtils.opusToPB(opus, xiaoji);
            updatePopScore();
		}

	}

	@Override
	public String toString() {
		return "NewCreateOpusService [uid=" + userId + ", appId=" + appId
				+ ", dataUrl=" + dataUrl + ", dataLen="
				+ dataLen + ", drawImageUrl=" + imageUrl 
				+ ", thumbImageUrl=" + thumbImageUrl + ", metaData="+pbOpus.toString()+"]";
	}

    private void updatePopScore() {
        if (xiaoji != null){
            double dataLength = dataLen;
            xiaoji.popUserManager().createOpus(userId, dataLength);
        }
    }

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {

        if (xiaoji == null){
            resultCode = ErrorCode.ERROR_XIAOJI_NULL;
            byteData = CommonServiceUtils.protocolBufferErrorNoData(resultCode);
            return false;
        }

		boolean zipDataFile = xiaoji.isZipUploadDataFile();
		ImageUploadManager dataUploadManager = xiaoji.getDataUploadManager();
		ImageUploadManager imageUploadManager = xiaoji.getImageUploadManager();
		
		uploadDataType = getStringValueFromRequeset(request, ServiceConstant.PARA_UPLOAD_DATA_TYPE, "dat");
		
		ParseResult result = UploadManager.readFormData(request, 
				ServiceConstant.PARA_OPUS_META_DATA, 
				ServiceConstant.PARA_OPUS_DATA,
				ServiceConstant.PARA_OPUS_IMAGE,
				uploadDataType, 
				imageUploadManager.getLocalDir(), 
				imageUploadManager.getRemoteDir(), 
				dataUploadManager.getLocalDir(), 
				dataUploadManager.getRemoteDir(), 
				zipDataFile);
		
		
		if (result != null) {
			
			byte[] metaData = result.getMetaData();
			
			dataUrl = result.getLocalDataFileUrl();
			dataLen = result.getDataLen();
			
			imageUrl = result.getLocalImageUrl();
			thumbImageUrl = result.getLocalThumbUrl();
			
			if (zipDataFile){
				// for DRAW here only
				// if isDrawApp xxx then do create compress file
				DataService.getInstance().createCompressDataFile(dataUrl);
			}
			
			// data, e.g. draw data, sing data
			if (dataUrl == null){
				resultCode = ErrorCode.ERROR_PARAMETER_DRAWDATA_NULL;
                byteData = CommonServiceUtils.protocolBufferErrorNoData(resultCode);
				return false;				
			}
						
			// parser meta data as PBOpus
			if (metaData != null && metaData.length > 0){
				try {
					pbOpus = PBOpus.parseFrom(metaData);
				} catch (InvalidProtocolBufferException e) {
					resultCode = ErrorCode.ERROR_PROTOCOL_BUFFER_PARSING;
                    byteData = CommonServiceUtils.protocolBufferErrorNoData(resultCode);
					return false;
				}
			}
			else{
				resultCode = ErrorCode.ERROR_PARAMETER_METADATA_NULL;
                byteData = CommonServiceUtils.protocolBufferErrorNoData(resultCode);
				return false;
			}

            String contestId = pbOpus.getContestId();
            if (!StringUtil.isEmpty(contestId)) {
                Contest contest = ContestManager.getContestById(mongoClient, contestId);
                if (contest != null){
                    if (!contest.canSubmit(userId)){
                        resultCode = ErrorCode.ERROR_CONTEST_EXCEED_SUBMIT_OPUS;
                        return false;
                    }
                }
            }


        } else {
			resultCode = ErrorCode.ERROR_PARAMETER_DRAWDATA_NULL;
            byteData = CommonServiceUtils.protocolBufferErrorNoData(resultCode);
			return false;
		}
		return true;
	}



}
