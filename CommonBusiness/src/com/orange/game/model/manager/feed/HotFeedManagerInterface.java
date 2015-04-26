package com.orange.game.model.manager.feed;

import java.util.List;

import org.bson.types.ObjectId;

import com.orange.game.model.dao.UserAction;

public interface HotFeedManagerInterface {
	
	public void cacheHotFeedList();
	public List<ObjectId> getFeedIds(int language, int offset, int limit);
	public void updateFeedIds(int language, UserAction action);
	public void deleteAction(String actionId, int language);
	public void updateAllFeeds(int language, List<UserAction> actionList);
	public boolean isCacheEmpty();
	public void loadDBCachedData();
}
