package com.orange.game.api.service.opus;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.service.opus.OpusService;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-6-4
 * Time: 下午2:34
 * To change this template use File | Settings | File Templates.
 */
public class SetOpusClassService extends CommonGameService {

    private static final int ACTION_ADD_OR_UPDATE = 1;
    private static final int ACTION_DELETE = 2;

    String opusId;
    List<String> classList;
    int type = ACTION_ADD_OR_UPDATE;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);
        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, ACTION_ADD_OR_UPDATE);

        String classListString = request.getParameter(ServiceConstant.PARA_CLASS);

        if (!check(opusId, ErrorCode.ERROR_PARAMETER_OPUSID_EMPTY, ErrorCode.ERROR_PARAMETER_OPUSID_NULL)){
            return false;
        }

        if (type == ACTION_ADD_OR_UPDATE){
            if (!check(classListString, ErrorCode.ERROR_PARAMETER_CLASS_EMPTY, ErrorCode.ERROR_PARAMETER_CLASS_NULL)){
                return false;
            }

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
        else{
            classList = new ArrayList<String>(); // empty list for delete
        }

        return true;
    }

    @Override
    public void handleData() {
        resultCode = OpusService.getInstance().updateOpusClass(opusId, classList);
    }

    @Override
    public String toString() {
        return "SetOpusClassService{" +
                "opusId='" + opusId + '\'' +
                ", classList=" + classList +
                '}';
    }
}
