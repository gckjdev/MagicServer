package com.orange.barrage.service.user;

import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.user.User;
import com.orange.barrage.model.user.UserManager;
import com.orange.game.api.service.ElasticsearchService;
import com.orange.game.constants.DBConstants;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by pipi on 15/1/22.
 */
public class SearchService {
    private static SearchService ourInstance = new SearchService();

    public static SearchService getInstance() {
        return ourInstance;
    }

    private SearchService() {
    }

    public int searchUserByKey(String userId, String searchKey, int offset, int limit,  MessageProtos.PBSearchUserResponse.Builder builder) {

        User user = new User();
        List<ObjectId> userIdList = ElasticsearchService.search(
                searchKey,
                user.fieldsForIndex(),
                BarrageConstants.F_USER_ID,
                offset,
                limit,
                user.getESIndexType()
                );

        List<User> userList = UserManager.findPublicUserInfoByUserIdList(userId, userIdList, false);
        if (userList != null) {
            List<UserProtos.PBUser> pbUserList = User.listToPB(userList, null);
            builder.addAllUsers(pbUserList);
        }

        return 0;
    }
}
