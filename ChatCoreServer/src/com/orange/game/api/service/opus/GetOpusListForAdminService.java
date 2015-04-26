package com.orange.game.api.service.opus;

import com.orange.common.utils.GsonUtils;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.opus.AllTimeTopOpusManager;
import com.orange.game.model.manager.opus.HotTopOpusManager;
import com.orange.game.model.manager.useropus.UserOpusManager;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;
import com.orange.network.game.protocol.message.GameMessageProtos;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * Created by chaoso on 14-6-23.
 */
public class GetOpusListForAdminService extends CommonGameService {

    static final int TYPE_HISTORY = 0;
    static final int TYPE_HOT = 1;
    static final int TYPE_USER_TOP = 2;
    static final int TYPE_SINGLE_USER = 3;

    int type = TYPE_HISTORY;
    int offset = 0;
    int limit = 24;

    String userXiaojiNumber;

    @Override
    public String toString() {
        return "GetOpusListForAdminService{" +
                "type=" + type +
                ", offset=" + offset +
                ", limit=" + limit +
                '}';
    }

    @Override
    public boolean setDataFromRequest(HttpServletRequest httpServletRequest) {

        type = getIntValueFromRequest(httpServletRequest, ServiceConstant.PARA_TYPE, TYPE_HISTORY);
        offset = getIntValueFromRequest(httpServletRequest, ServiceConstant.PARA_OFFSET, 0);
        limit = getIntValueFromRequest(httpServletRequest, ServiceConstant.PARA_LIMIT, 24);
        userXiaojiNumber = httpServletRequest.getParameter(ServiceConstant.PARA_XIAOJI_NUMBER);
        return true;
    }

    @Override
    public void handleData() {

        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getDraw();

        List<UserAction> opusList = Collections.emptyList();

        switch (type) {
            case TYPE_HISTORY: {
                AllTimeTopOpusManager opusManager = xiaoji.allTimeTopOpusManager(DBConstants.C_LANGUAGE_CHINESE);
                opusList = opusManager.getTopListFromMongoDB(offset, limit);   // change by Benson 2014-07-03
            }
                break;

            case TYPE_HOT: {
                HotTopOpusManager opusManager = xiaoji.hotTopOpusManager(DBConstants.C_LANGUAGE_CHINESE);
                opusList = opusManager.getTopList(offset, limit);
            }
                break;

            case TYPE_USER_TOP: {
                // TODO
            }
                break;

            case TYPE_SINGLE_USER: {
                UserOpusManager opusManager = xiaoji.userOpusManager();
                User user = UserManager.findUserByXiaojiNumber(userXiaojiNumber);
                if (user != null) {
                    opusList = opusManager.getList(user.getUserId(), offset, limit, null, null, 0);
                }
                else{
                    resultCode = ErrorCode.ERROR_USER_NOT_FOUND;
                }
            }
                break;
        }

        if (opusList.size() > 0) {

//            for (UserAction opus : opusList){
//                if (opus.getClassList().size() > 0){
//                    opusList.remove(opus);
//                }
//            }

            GameMessageProtos.DataQueryResponse response = CommonServiceUtils.feedListToDataQueryResponse(opusList, 0, false, false, false);
            resultData = GsonUtils.toJSON(response);
        }

        log.debug("<GetOpusListForAdminService> response=" + resultData);

//        HotTopOpusManager opusManager = xiaoji.hotTopOpusManager(DBConstants.C_LANGUAGE_CHINESE);


    }
}
