package com.orange.game.api.barrage.common;

import com.orange.game.api.barrage.service.barrage.*;
import com.orange.game.api.barrage.service.chat.GetChatListService;
import com.orange.game.api.barrage.service.chat.SendChatService;
import com.orange.game.api.barrage.service.misc.FeedbackService;
import com.orange.game.api.barrage.service.user.LoginUserService;
import com.orange.game.api.barrage.service.user.RegisterUserService;
import com.orange.game.api.barrage.service.user.SearchUserService;
import com.orange.game.api.barrage.service.user.UpdateUserInfoService;
import com.orange.game.api.barrage.service.user.friend.DeleteFriendService;
import com.orange.game.api.barrage.service.user.invitecode.*;
import com.orange.game.api.barrage.service.user.friend.AddUserFriendService;
import com.orange.game.api.barrage.service.user.friend.GetUserFriendListService;
import com.orange.game.api.barrage.service.user.friend.ProcessUserFriendService;
import com.orange.game.api.barrage.service.user.tag.AddUserTagService;
import com.orange.game.api.barrage.service.user.tag.DeleteUserTagService;
import com.orange.game.api.barrage.service.user.tag.GetUserTagListService;
import com.orange.game.api.service.CommonGameService;
import com.orange.protocol.message.ErrorProtos;
import com.orange.protocol.message.MessageProtos;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by pipi on 14/12/1.
 */
public class CommonDataRequestService extends CommonGameService {

    // please add class/sevice mapping here
    private CommonBarrageService getService(int type){
        switch (type){
            case MessageProtos.PBMessageType.MESSAGE_LOGIN_USER_VALUE:
                return LoginUserService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_REGISTER_USER_VALUE:
                return RegisterUserService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_UPDATE_USER_INFO_VALUE:
                return UpdateUserInfoService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_CREATE_FEED_VALUE:
                return CreateFeedService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_GET_USER_TIMELINE_FEED_VALUE:
                return GetFeedListService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_REPLY_FEED_VALUE:
                return ReplyFeedService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_VERIFY_INVITE_CODE_VALUE:
                return VerifyInviteCodeService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_GET_NEW_INVITE_CODE_VALUE:
                return GetNewInviteCodeService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_GET_USER_FRIEND_LIST_VALUE:
                return GetUserFriendListService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_ADD_USER_FRIEND_VALUE:
                return AddUserFriendService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_PROCESS_USER_FRIEND_VALUE:
                return ProcessUserFriendService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_ADD_USER_TAG_VALUE:
                return AddUserTagService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_DELETE_USER_TAG_VALUE:
                return DeleteUserTagService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_GET_USER_TAG_LIST_VALUE:
                return GetUserTagListService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_SEARCH_USER_VALUE:
                return SearchUserService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_GET_USER_INVITE_CODE_LIST_VALUE:
                return GetUserInviteCodeListService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_APPLY_INVITE_CODE_VALUE:
                return ApplyInviteCodeService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_UPDATE_INVITE_CODE_VALUE:
                return UpdateInviteCodeService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_DELETE_FEED_ACTION_VALUE:
                return DeleteFeedActionService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_DELETE_FEED_VALUE:
                return DeleteFeedService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_DELETE_FRIEND_VALUE:
                return DeleteFriendService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_SEND_USER_FEEDBACK_VALUE:
                return FeedbackService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_GET_MY_NEW_FEED_LIST_VALUE:
                return GetMyNewFeedService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_GET_FEED_BY_ID_VALUE:
                return GetFeedByIdService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_READ_MY_NEW_FEED_VALUE:
                return ReadMyNewFeedService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_GET_USER_FEED_VALUE:
                return GetUserFeedService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_SEND_CHAT_VALUE:
                return SendChatService.getInstance();

            case MessageProtos.PBMessageType.MESSAGE_GET_CHAT_LIST_VALUE:
                return GetChatListService.getInstance();


        }

        log.warn("<getService> but unknown message type "+type+" received");
        return null;
    }

    byte[] data;
    MessageProtos.PBDataRequest dataRequest;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        try {
            data = readPostData(request.getInputStream());
        } catch (IOException e) {
            resultCode = ErrorProtos.PBError.ERROR_READ_POST_DATA_VALUE;
            log.error("catch exception while read post data, exception="+e.toString(), e);
            return true;
        }

        try {
            dataRequest = MessageProtos.PBDataRequest.parseFrom(data);
        } catch (Exception e) {
            int length = (data == null) ? 0 : data.length;
            resultCode = ErrorProtos.PBError.ERROR_PARSE_POST_DATA_VALUE;
            log.error("catch exception while parse post data, exception="+e.toString()+", data bytes="+length, e);
            return true;
        }

        // TODO check others

        return true;
    }

    public MessageProtos.PBDataResponse responseWithErrorCode(int errorCode){
        MessageProtos.PBDataResponse.Builder responseBuilder = MessageProtos.PBDataResponse.newBuilder();
        responseBuilder.setRequestId(0);
        responseBuilder.setResultCode(errorCode);
        MessageProtos.PBDataResponse response = responseBuilder.build();
        return response;
    }

    @Override
    public void handleData() {

        if (resultCode != 0){
            // this is for prehandling check
            MessageProtos.PBDataResponse response = responseWithErrorCode(resultCode);
            log.warn("prehandle request fails, result code is "+resultCode);
            log.info("[SEND] response = "+response.toString());
            byteData = response.toByteArray();
            return;
        }

        log.info("[RECV] request = "+dataRequest.toString());

        MessageProtos.PBDataResponse.Builder responseBuilder = MessageProtos.PBDataResponse.newBuilder();
        responseBuilder.setRequestId(dataRequest.getRequestId());
        responseBuilder.setResultCode(0);

        // process request here
        processBarrageRequest(dataRequest, responseBuilder);

        MessageProtos.PBDataResponse response = responseBuilder.build();
        log.info("[SEND] response = "+response.toString());
        byteData = response.toByteArray();
    }

    private void processBarrageRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        CommonBarrageService service = getService(dataRequest.getType());
        if (service == null){
            responseBuilder.setRequestId(ErrorProtos.PBError.ERROR_NO_SERVICE_FOR_TYPE_VALUE);
            return;
        }
        else{
            log.info("process service "+service.getClass().getName());
        }

        try {

            if (service.validateRequest(dataRequest, responseBuilder) == false){
                log.warn("process service but validateRequest fails!");
                resultCode = ErrorProtos.PBError.ERROR_READ_POST_DATA_VALUE;
                return;
            }

            service.handleRequest(dataRequest, responseBuilder);
        }catch (Exception e){
            responseBuilder.setRequestId(dataRequest.getRequestId());
            responseBuilder.setResultCode(ErrorProtos.PBError.ERROR_SERVICE_CATCH_EXCEPTION_VALUE);
            log.error("<processBarrageRequest> catch exception="+e.toString(), e);
            return;
        }
    }
}
