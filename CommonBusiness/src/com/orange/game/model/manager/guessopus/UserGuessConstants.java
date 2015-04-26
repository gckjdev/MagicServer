package com.orange.game.model.manager.guessopus;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-8-5
 * Time: 下午5:22
 * To change this template use File | Settings | File Templates.
 */
public class UserGuessConstants {

    public static final String MODE_HAPPY = "happy";
    public static final String MODE_GENIUS = "genius";
    public static final String MODE_CONTEST = "contest";

    public static final String USER_GUESS_PREFIX = "user_guess_";

    public static final int HOT_RANK = 0;
    public static final int ALL_TIME_RANK = 1;

    public static final int MODE_HAPPY_MIN_DATA_LEN = 10000;
    public static final int MODE_GENIUS_MIN_DATA_LEN = 50000;
    public static final int MODE_CONTEST_MIN_DATA_LEN = 100000;

//    public static final int MODE_HAPPY_MIN_DATA_LEN = 0;
//    public static final int MODE_GENIUS_MIN_DATA_LEN = 0;
//    public static final int MODE_CONTEST_MIN_DATA_LEN = 0;

    public static final int TOTAL_OPUS_NUMBER_IN_CONTEST = 50;
    public static final float CONTEST_AWARD_RATE = (float) 0.15;
    public static final int CONTEST_COST_COIN = 100;
    public static final int CONTEST_OPUS_AUTHOR_AWARD = 20;
    public static final int CONTEST_SYSTEM_COST = 10;

    public static final float CONTEST_NUMBER_ONE_AWARD_RATE = (float) 0.5;
    public static final float CONTEST_NUMBER_TWO_AWARD_RATE = (float) 0.3;
//    public static final float CONTEST_NUMBER_THREE_AWARD_RATE = (float) 0.1;
    public static final float CONTEST_NUMBER_THREE_AWARD_RATE = (float) 0.2;
    public static final float CONTEST_NUMBER_FOUR_AWARD_RATE = (float) 0.1;
    public static final long MIN_AWARD_USER_COUNT = 10;

    public static final int CONTEST_START_HOUR = 18;
    public static final int CONTEST_END_HOUR = 23;
}
