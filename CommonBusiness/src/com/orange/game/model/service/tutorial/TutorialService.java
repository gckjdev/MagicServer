package com.orange.game.model.service.tutorial;

/**
 * Created by chaoso on 14-7-11.
 */
public class TutorialService {

    private static TutorialService ourInstance = new TutorialService();

    public static TutorialService getInstance() {
        return ourInstance;
    }

    private TutorialService() {
    }
}
