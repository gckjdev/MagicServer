package com.orange.barrage.service.user;

import com.orange.barrage.model.user.UserTagManager;
import com.orange.protocol.message.ErrorProtos;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 15/1/20.
 */
public class UserTagService {

    private static UserTagService ourInstance = new UserTagService();

    public static UserTagService getInstance() {
        return ourInstance;
    }

    private UserTagService() {
    }

    public int addUserTag(String userId, UserProtos.PBUserTag userTag, MessageProtos.PBAddUserTagResponse.Builder builder) {

        UserProtos.PBUserTagList userTagList = UserTagManager.getInstance().createNewTag(userId, userTag);
        if (userTagList != null && userTagList.getTagsCount() > 0){
            builder.setTags(userTagList);
            return 0;
        }
        else{
            return ErrorProtos.PBError.ERROR_USER_TAG_LIST_NULL_VALUE;
        }
    }

    public int deleteUserTag(String userId, UserProtos.PBUserTag userTag, MessageProtos.PBDeleteUserTagResponse.Builder builder) {
        UserProtos.PBUserTagList userTagList = UserTagManager.getInstance().deleteTag(userId, userTag);
        if (userTagList != null){
            builder.setTags(userTagList);
            return 0;
        }
        else{
            return ErrorProtos.PBError.ERROR_USER_TAG_LIST_NULL_VALUE;
        }
    }

    public int getUserTagList(String userId, MessageProtos.PBGetUserTagListResponse.Builder builder) {
        UserProtos.PBUserTagList userTagList = UserTagManager.getInstance().getUserTagList(userId, true);
        if (userTagList != null){
            builder.setTags(userTagList);
            return 0;
        }
        else{
            return 0;
//            return ErrorProtos.PBError.ERROR_USER_TAG_LIST_NULL_VALUE;
        }
    }
}
