package com.orange.barrage.service.push;

import com.orange.common.utils.StringUtil;
import org.apache.log4j.Logger;

/**
 * Created by pipi on 15/4/17.
 */
public class PushService  {

    private static PushService ourInstance = new PushService();

    public static PushService getInstance() {
        return ourInstance;
    }

    protected static Logger log = Logger.getLogger(PushService.class.getName());

    MiPushManager androidManager;
    MiPushManager iOSManager;

    private PushService() {
        iOSManager = new MiPushManager("z9PH0haNBsxcVVw2ahUzIA==");
    }

    public void sendMessage(int deviceType, String userId, String description, int badge){

        if (StringUtil.isEmpty(userId)){
            log.warn("<sendMessage> but regId is empty or null");
            return;
        }

        if (deviceType == 0) {
            iOSManager.setMode(true);
            try {
                log.info("<sendMessage> regId=" + userId + ", description=" + description + ", badge=" + badge);
                iOSManager.sendMessage(userId, description, badge);
            } catch (Exception e) {
                log.error("<sendMessage> regId=" + userId + " but catch exception=" + e.toString(), e);
            }
        }
        else{
            // TODO android push
        }
    }
}
