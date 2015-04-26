package com.orange.game.model.generator;

import java.util.List;

import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.user.TopUserManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.XiaojiFactory;

public class TopUserDataGenerator {
			
	public static void createTopUserData(){
		for (int language=1; language<=2; language++){
			int topUserCount = TopUserManager.MAX_TOP_USER_COUNT;
			int i = 0;
			int offset = 0;
			int limit = 50;
			while (i<topUserCount){
				List<UserAction> userActions = OpusManager.getHistoryTopOpusList(DBService.getInstance().getMongoDBClient(), "", language, offset, limit, true);
				if (userActions.size() == 0)
					break;
				
				for (UserAction userAction : userActions){
					XiaojiFactory.getInstance().getDraw().topUserManager().updateUserScore(
							userAction.getCreateUserId(),
							userAction.getOpusId(),
							userAction.getHistoryScore(),
							false);				
				}
				
				i = XiaojiFactory.getInstance().getDraw().topUserManager().getTopUserCount();
				offset += limit;
			}
		}
	}
}
