package com.orange.game.api.service.guessopus;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.opus.UserGuessAchievement;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.guessopus.TopUserGuessManager;
import com.orange.game.model.service.DBService;
import com.orange.network.game.protocol.model.OpusProtos;
import org.bson.types.ObjectId;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-7-17
 * Time: 下午1:30
 * To change this template use File | Settings | File Templates.
 */
public class GetUserGuessRankListService extends CommonGameService {

    int type;
    int mode;
    String contestId;
    int offset;
    int limit;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, -1);

        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, -1);
        contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);

        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT, 20);

        if (mode == -1){
            resultCode = ErrorCode.ERROR_USER_GUESS_MODE;
            return  false;
        }

        return true;
    }

    @Override
    public void handleData() {

        // TODO support contest later

        TopUserGuessManager manager = xiaoji.getTopUserGuessManager(mode, type, contestId);

        if (manager == null){
            resultCode = ErrorCode.ERROR_USER_GUESS_MODE;
            byteData = protocolBufferWithErrorCode(resultCode);
            return;
        }


        List<UserGuessAchievement> list = manager.getAchievementList(offset, limit);

        List<ObjectId> userIdList = new ArrayList<ObjectId>();
        for (UserGuessAchievement achievement : list){
            userIdList.add(achievement.getUserId(manager.getAchievementKey()));
        }

//        List<User> users = UserManager.findPublicUserInfoByUserIdList(DBService.getInstance().getMongoDBClient(), userIdList);
//        Map<String, User> userMap = new HashMap<String, User>();
//        for (User user : users){
//            userMap.put(user.getUserId(), user);
//        }

        byteData = CommonServiceUtils.achievementListToPB(list, manager);
    }
}