package com.orange.game.model.xiaoji;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.opus.Opus;
import com.orange.game.model.manager.opus.LatestOpusManager;
import com.orange.network.game.protocol.model.OpusProtos;

public class XiaojiAskPs extends AbstractXiaoji {

	@Override
	public LatestOpusManager latestOpusManager(String appId, int language) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCategoryName() {
		return "askps";
	}

	@Override
	public int getCategoryType() {
		// TODO Auto-generated method stub
		return DBConstants.C_CATEGORY_TYPE_ASKPS;
	}

	@Override
	public boolean isZipUploadDataFile() {
		return false;
	}

    @Override
    public void setOpusInfo(Opus opus, OpusProtos.PBOpus pbOpus) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void opusToPB(UserAction opus, OpusProtos.PBOpus.Builder builder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public double calculateAndSetHistoryScore(UserAction action) {
        log.warn("<calculateAndSetHistoryScore> NOT IMPLEMENTED");
        return 0;
    }

    @Override
    public double calculateHotScore(UserAction action){
        log.warn("<calculateAndSetHistoryScore> NOT IMPLEMENTED");
        return 0;
    }

    @Override
    public boolean isOneUserOneOpusForLatest() {
        return true;
    }

}
