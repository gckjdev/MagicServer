package com.orange.game.model.xiaoji;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.opus.Opus;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.ScoreManager;
import com.orange.game.model.manager.opus.LatestOpusManager;
import com.orange.game.model.manager.utils.ImageUploadManager;
import com.orange.game.model.service.DataService;
import com.orange.network.game.protocol.model.OpusProtos;

public class XiaojiDraw extends AbstractXiaoji {

	
	
	final LatestOpusManager caicaihuahuaCnLatestOpusManager = new LatestOpusManager(getCategoryName(), DBConstants.C_SUB_CATEGORY_CAICAIHUHUA_CN);
	final LatestOpusManager caicaihuahuaEnLatestOpusManager = new LatestOpusManager(getCategoryName(), DBConstants.C_SUB_CATEGORY_CAICAIHUHUA_EN);
	final LatestOpusManager xiaojihuahuaLatestOpusManager = new LatestOpusManager(getCategoryName(), DBConstants.C_SUB_CATEGORY_XIAOJIHUAHUA);
	
	
	@Override
	public String getCategoryName() {
		return "draw";
	}

	public int getCategoryType(){
		return DBConstants.C_CATEGORY_TYPE_DRAW;
	}

	@Override
	public LatestOpusManager latestOpusManager(String appId, int language) {
		
		if (appId.equals(DBConstants.APPID_LITTLEGEE))
			return xiaojihuahuaLatestOpusManager;

		if (appId.equalsIgnoreCase(DBConstants.APPID_DRAW)){
			if (language == DBConstants.C_LANGUAGE_ENGLISH){
				return caicaihuahuaEnLatestOpusManager;
			}
			else{
				return caicaihuahuaCnLatestOpusManager;				
			}
		}
				
		return caicaihuahuaCnLatestOpusManager;
	}

	@Override
	public boolean isZipUploadDataFile() {
		return true;
	}

    @Override
    public void setOpusInfo(Opus opus, OpusProtos.PBOpus pbOpus) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void opusToPB(UserAction opus, OpusProtos.PBOpus.Builder builder){
        // TODO
    }

    @Override
    public double calculateAndSetHistoryScore(UserAction action) {
        return ScoreManager.calculateAndSetHistoryDrawScore(action);
    }

    @Override
    public double calculateHotScore(UserAction action) {
        return ScoreManager.calculateHotDrawScore(action);
    }


    @Override
    public boolean isOneUserOneOpusForLatest() {
        return true;
    }

}
