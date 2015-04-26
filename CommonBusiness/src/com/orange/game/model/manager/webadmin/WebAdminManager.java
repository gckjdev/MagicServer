package com.orange.game.model.manager.webadmin;

/**
 * Created by chaoso on 14-6-25.
 */
public class WebAdminManager {
    private static WebAdminManager ourInstance = new WebAdminManager();

    public static WebAdminManager getInstance() {
        return ourInstance;
    }

    private WebAdminManager() {
    }

    public static boolean adminLog(String userName, String password){
        return true;
    }
}
