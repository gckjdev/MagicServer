package com.orange.game.api.service.contest;

import com.google.protobuf.InvalidProtocolBufferException;
import com.orange.common.upload.UploadManager;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.manager.utils.ImageUploadManager;
import com.orange.game.model.service.DataService;
import com.orange.network.game.protocol.model.GroupProtos;
import com.orange.network.game.protocol.model.OpusProtos;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 14-1-2
 * Time: 下午4:25
 * To change this template use File | Settings | File Templates.
 */
public class CreateContestService extends CommonGameService {

    String imageUrl;
    String thumbImageUrl;
    GroupProtos.PBContest pbContest;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (xiaoji == null){
            resultCode = ErrorCode.ERROR_XIAOJI_NULL;
            byteData = CommonServiceUtils.protocolBufferErrorNoData(resultCode);
            return false;
        }

        ImageUploadManager imageUploadManager = xiaoji.getContestImageUploadManager();

        UploadManager.ParseResult result = UploadManager.readFormData(request,
                ServiceConstant.PARA_META_DATA,
                null,
                ServiceConstant.PARA_IMAGE,
                null,
                imageUploadManager.getLocalDir(),
                imageUploadManager.getRemoteDir(),
                null,
                null,
                false);


        if (result != null) {

            byte[] metaData = result.getMetaData();

            imageUrl = result.getLocalImageUrl();
            thumbImageUrl = result.getLocalThumbUrl();

            // parser meta data
            if (metaData != null && metaData.length > 0){
                try {
                    pbContest = GroupProtos.PBContest.parseFrom(metaData);
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

        } else {
            resultCode = ErrorCode.ERROR_PARAMETER_DRAWDATA_NULL;
            byteData = CommonServiceUtils.protocolBufferErrorNoData(resultCode);
            return false;
        }
        return true;
    }

    @Override
    public void handleData() {

        Contest contest = ContestManager.createContest(pbContest, thumbImageUrl, imageUrl);
        if (contest == null){
            resultCode = ErrorCode.ERROR_CONTEST_CREATE_FAIL;
        }

        byteData = CommonServiceUtils.protocolBufferErrorNoData(resultCode);
    }
}
