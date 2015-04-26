package com.orange.game.test.model.feed.contest;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.feed.ContestFeedManager;

public class ContestIndexTest {

	MongoDBClient mongoClient = null;
	
	@Before
	public void setUp() throws Exception {
		mongoClient = new MongoDBClient(DBConstants.D_GAME);
	}

	@After
	public void tearDown() throws Exception {
	}

//	@Test
	public void testInsertContestFeed1() {
		UserAction userAction = new UserAction(new BasicDBObject());
		userAction.setContestId("test_contest_id_1");
		userAction.setCreateUserId("test_create_user_id_1");
		userAction.setType(UserAction.TYPE_DRAW_TO_CONTEST);
		mongoClient.save(DBConstants.T_OPUS, userAction.getDbObject());

		ContestFeedManager.getInstance().insertContestFeed(mongoClient, userAction);
	}

//	@Test
	public void testGetContestFeed() {
		String contestId = "test_contest_id_1";
		String userId = "test_create_user_id_1";
		ContestFeedManager.getInstance().getContestUserOpusIds(mongoClient, contestId, userId, 0, 10);
		ContestFeedManager.getInstance().getLatestContestOpusIds(mongoClient, contestId, 0, 10);
	}
		
//	@Test
	public void testInsertContestFeed2() {
		
		// another user draw for the contest
		
		UserAction userAction = new UserAction(new BasicDBObject());
		userAction.setContestId("test_contest_id_1");
		userAction.setCreateUserId("test_create_user_id_2");
		userAction.setType(UserAction.TYPE_DRAW_TO_CONTEST);
		mongoClient.save(DBConstants.T_OPUS, userAction.getDbObject());
		
		ContestFeedManager.getInstance().insertContestFeed(mongoClient, userAction);
	}

//	@Test
	public void testInsertContestFeed3() {
		
		// user draw one more for the contest
		
		UserAction userAction = new UserAction(new BasicDBObject());
		userAction.setContestId("test_contest_id_1");
		userAction.setCreateUserId("test_create_user_id_1");
		userAction.setType(UserAction.TYPE_DRAW_TO_CONTEST);
		mongoClient.save(DBConstants.T_OPUS, userAction.getDbObject());
		
		ContestFeedManager.getInstance().insertContestFeed(mongoClient, userAction);
	}

//	@Test
	public void testInsertContestFeed4() {
		
		// user draw one more for the another contest
		
		UserAction userAction = new UserAction(new BasicDBObject());
		userAction.setContestId("test_contest_id_2");
		userAction.setCreateUserId("test_create_user_id_1");
		userAction.setType(UserAction.TYPE_DRAW_TO_CONTEST);
		mongoClient.save(DBConstants.T_OPUS, userAction.getDbObject());
		
		ContestFeedManager.getInstance().insertContestFeed(mongoClient, userAction);
	}
}
