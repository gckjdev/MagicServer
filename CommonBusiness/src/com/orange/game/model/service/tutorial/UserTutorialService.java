package com.orange.game.model.service.tutorial;

/**
 * Created by chaoso on 14-7-11.
 */
public class UserTutorialService {
    private static UserTutorialService ourInstance = new UserTutorialService();

    public static UserTutorialService getInstance() {
        return ourInstance;
    }

    private UserTutorialService() {
    }
}
