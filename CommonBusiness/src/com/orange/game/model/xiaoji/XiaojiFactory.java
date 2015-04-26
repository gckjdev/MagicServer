package com.orange.game.model.xiaoji;

import java.util.HashMap;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.App;
import com.orange.game.model.dao.app.AbstractApp;
import com.orange.game.model.dao.app.AppFactory;

public class XiaojiFactory {

	
	static XiaojiFactory defaultFactory = new XiaojiFactory(); 
	
	final XiaojiDraw draw = new XiaojiDraw();
	final XiaojiSing sing = new XiaojiSing();
	final XiaojiAskPs askPs = new XiaojiAskPs();

	
	final HashMap<Integer, AbstractXiaoji> map = new HashMap<Integer, AbstractXiaoji>(); 
	
	public static XiaojiFactory getInstance(){
		return defaultFactory;
	}
	
	private XiaojiFactory(){		
		map.put(draw.getCategoryType(), draw);
		map.put(sing.getCategoryType(), sing);
		map.put(askPs.getCategoryType(), askPs);
	}
	
	public AbstractXiaoji getXiaoji(String appId){
		AbstractApp app = AppFactory.getInstance().getApp(appId);
		if (app == null)
			return null;
		
		return app.getXiaoji();
	}

    public AbstractXiaoji getXiaojiByGameId(String gameId){

        int categoryType;
        if (gameId != null){
            if (gameId.equalsIgnoreCase(DBConstants.GAME_ID_ASK_PS))
                categoryType = DBConstants.C_CATEGORY_TYPE_ASKPS;
            else if (gameId.equalsIgnoreCase(DBConstants.GAME_ID_SING))
                categoryType = DBConstants.C_CATEGORY_TYPE_SING;
            else if (gameId.equalsIgnoreCase(DBConstants.GAME_ID_DRAW))
                categoryType = DBConstants.C_CATEGORY_TYPE_DRAW;
            else
                return null;

            return getXiaoji(categoryType);
        }
        else{
            return null;
        }
    }


    public AbstractXiaoji getDraw() {
		return draw;
	}
	
	public AbstractXiaoji getSing() {
		return sing;
	}
	
	public AbstractXiaoji getAskPs() {
		return sing;
	}

	public AbstractXiaoji getXiaoji(int categoryType) {
		if (map.containsKey(categoryType))
			return map.get(categoryType);
		else {
			return null;
		}
	}

    public boolean isDraw(String category) {

        if (category == null){
            return true;
        }

        if (category.equalsIgnoreCase(getDraw().getCategoryName())){
            return true;
        }
        else{
            return false;
        }
    }
}
