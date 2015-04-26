package com.orange.barrage.service.yun.qiniu;

import com.orange.barrage.common.CommonModelService;
import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.PutPolicy;
import org.json.JSONException;

/**
 * Created by pipi on 14/12/8.
 */
public class QiuNiuService extends CommonModelService {
    private static QiuNiuService ourInstance = new QiuNiuService();

    public static QiuNiuService getInstance() {
        return ourInstance;
    }

    public static String ACCESS_KEY = "PGXicdkeGqQHXTBCV-qKbMaQj6aFWwM3yS1qcKKF";
    public static String SECRET_KEY = "jOYD52jvAnqfMg1WgvVeIcqFeJcW9gLhSewYNZ8r";
    public static String BUCKET_NAME = "gckjdev";
    public static String UPLOAD_TOKEN = "PGXicdkeGqQHXTBCV-qKbMaQj6aFWwM3yS1qcKKF:9Xf3WPpvNVq2-MWiyQE-xw6IZHM=:eyJzY29wZSI6Imdja2pkZXYiLCJkZWFkbGluZSI6MTQxODAzODg4MH0=";

    private QiuNiuService() {
    }

    public String getUpToken(){
        Config.ACCESS_KEY = ACCESS_KEY;
        Config.SECRET_KEY = SECRET_KEY;

        Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
        String bucketName = BUCKET_NAME;
        PutPolicy putPolicy = new PutPolicy(bucketName);
        putPolicy.expires = System.currentTimeMillis()/1000 + 365*24*60*60;
        try {
            String uptoken = putPolicy.token(mac);
            log.info("upload token is "+uptoken);
            return uptoken;
        } catch (AuthException e) {
            log.error("QiNiu getUpToken but catch exception="+e.toString(), e);
            return null;
        } catch (JSONException e) {
            log.error("QiNiu getUpToken but catch exception="+e.toString(), e);
            return null;
        }
    }

}
