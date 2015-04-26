package com.orange.barrage.service.user;

/**
 * Created by pipi on 14/12/2.
 */
public class ShareUserService {
    private static ShareUserService ourInstance = new ShareUserService();

    public static ShareUserService getInstance() {
        return ourInstance;
    }

    private ShareUserService() {
    }
}
