package com.orange.game.model.dao;

import java.util.Date;

import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;

public class Bulletin extends CommonData {

    public final static int JumpTypeCanotJump = 0;
    public final static int JumpTypeGame = 1; // game page
    public final static int JumpTypeIntegral = 2; // Integral
    public final static int JumpTypeWeb = 3; //webview
    public final static int JumpTypeSafari = 4; //safari
    public final static int JumpTypeOfferWall = 5;

    public final static String FUNC_OPUS_DETAIL               = "opus_detail";
    public final static String FUNC_USER_DETAIL               = "user_detail";
    public final static String FUNC_FEED                      = "feed";
    public final static String FUNC_CONTEST                   = "contest";
    public final static String FUNC_TOP                       = "top";
    public final static String FUNC_FREE_COINS                = "free_coins";
    public final static String FUNC_FREE_INGOT                = "free_ingot";
    public final static String FUNC_SHOP                      = "shop";
    public final static String FUNC_BBS_FREE_COIN_HELP        = "bbs_free_ingot";
    public final static String FUNC_BBS_FEEDBACK              = "bbs_feedback";
    public final static String FUNC_BBS_BUG_REPORT            = "bbs_bug_report";

	public Bulletin() {
		super();
	}
	
	public Bulletin(DBObject object) {
		super(object);
	}
	
	public String getBulletinId() {
		return getObjectId().toString();
	}
	
	public Date getCreateDate() {
		return getDate(DBConstants.F_DATE);
	}
	
	public int getType() {
		return getInt(DBConstants.F_TYPE); 
	}
	
	public String getGameId() {
		return getString(DBConstants.F_GAME_ID);
	}
	
	public String getFunction() {
		return getString(DBConstants.F_FUNCTION);
	}
	
	public String getMessagge() {
		return getString(DBConstants.F_CONTENT);
	}
	
	public String getUrl() {
		return getString(DBConstants.F_URL);
	}

    public String getFunctionParameter() {
        return getString(DBConstants.F_FUNC_PARA);
    }

}
