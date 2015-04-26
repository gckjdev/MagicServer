package com.orange.game.model.manager.bbs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.orange.common.utils.StringUtil;
import com.orange.game.model.dao.bbs.BBSBoard;
import com.orange.game.model.manager.CommonManager;
import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;
import org.hamcrest.core.IsInstanceOf;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.App;
import com.orange.game.model.dao.Board;
//import com.sun.org.apache.regexp.internal.recompile;

public class BoardManager extends CommonManager {

	private static Board createLocalWebBoard() {
		Board board = new Board(new BasicDBObject());
		board.setIndex(2);

		List<Integer> deviceTypes = new ArrayList<Integer>();
		deviceTypes.add(Board.BoardDeviceTypeIPad);
		deviceTypes.add(Board.BoardDeviceTypeIPhone);

		board.setDeviceType(deviceTypes);

		List<String> gameIds = new ArrayList<String>();
		// gameIds.add(App.getDiceGameId());
		
		// rem by Benson 2013-04-27
//		gameIds.add(App.getDrawGameId());
		board.setGameIds(gameIds);

		board.setStatus(Board.BoardStatusRun);
		board.setVersion("2.0");
		board.setType(Board.BoardTypeWeb);
		board.setWebType(Board.WebTypeLocal);
		board.setRemoteUr("http://192.167.1.123/test.zip");
		board.setLocalUrl("test.html");
		return board;
	}

	private static Board createRemoteWebBoard() {
		Board board = new Board(new BasicDBObject());
		board.setIndex(1);

		List<Integer> deviceTypes = new ArrayList<Integer>();
		deviceTypes.add(Board.BoardDeviceTypeIPad);
		deviceTypes.add(Board.BoardDeviceTypeIPhone);
		board.setDeviceType(deviceTypes);

		List<String> gameIds = new ArrayList<String>();
		// rem by Benson 2013-04-27		
//		gameIds.add(App.getDiceGameId());
		// gameIds.add(App.getDrawGameId());
		board.setGameIds(gameIds);

		board.setStatus(Board.BoardStatusRun);
		board.setVersion("1");
		board.setType(Board.BoardTypeWeb);
		board.setWebType(Board.WebTypeRemote);
		board.setRemoteUr("http://192.167.1.123");
		// board.setRemoteUr("http://place100.com:8080/draw_image/20120816/62fa4ae0-e7b6-11e1-90f6-00163e017466.jpg");
		return board;
	}

	private static Board createImageBoard() {
		Board board = new Board(new BasicDBObject());
		board.setIndex(0);

		List<Integer> deviceTypes = new ArrayList<Integer>();
		deviceTypes.add(Board.BoardDeviceTypeIPad);
		deviceTypes.add(Board.BoardDeviceTypeIPhone);
		board.setDeviceType(deviceTypes);

		List<String> gameIds = new ArrayList<String>();
		// rem by Benson 2013-04-27
//		gameIds.add(App.getDiceGameId());
//		gameIds.add(App.getDrawGameId());
		board.setGameIds(gameIds);

		board.setStatus(Board.BoardStatusRun);
		board.setVersion("1");
		board.setType(Board.BoardTypeImage);
		board.setImageUrl("http://192.167.1.123/BoardControl/image.png");
		board.setAdImageUrl("http://192.167.1.123/BoardControl/ad_image.png");

		board.setClickUrl("tgb://board?type=1&game=draw&func=feed");

		board.setAdPlatform(Board.AdPlatformAder);
		board.setAdPublishId("3b47607e44f94d7c948c83b7e6eb800e");

		return board;
	}

	private static Board createDefaultBoard() {
		Board board = new Board(new BasicDBObject());
		board.setIndex(-1);

		List<Integer> deviceTypes = new ArrayList<Integer>();
		deviceTypes.add(Board.BoardDeviceTypeIPad);
		deviceTypes.add(Board.BoardDeviceTypeIPhone);
		board.setDeviceType(deviceTypes);

		List<String> gameIds = new ArrayList<String>();
		// rem by Benson 2013-04-27		
//		gameIds.add(App.getDiceGameId());
//		gameIds.add(App.getDrawGameId());
		board.setGameIds(gameIds);

		board.setStatus(Board.BoardStatusRun);
		board.setVersion("1");
		board.setType(Board.BoardTypeDefault);
		return board;
	}

	// for test
	private static List<Board> getTestBoardList(MongoDBClient mongoClient,
			String appId) {

		log.info("getTestBoardList");
		List<Board> boardList = new ArrayList<Board>();
		Board board = createRemoteWebBoard();
		mongoClient.insert(DBConstants.T_BOARD, board.getDbObject());
		boardList.add(board);
		log.info("insert board = " + board.getDbObject());

		board = createImageBoard();
		mongoClient.insert(DBConstants.T_BOARD, board.getDbObject());
		boardList.add(board);
		log.info("insert board = " + board.getDbObject());

		board = createLocalWebBoard();
		mongoClient.insert(DBConstants.T_BOARD, board.getDbObject());
		boardList.add(board);
		log.info("insert board = " + board.getDbObject());

		board = createDefaultBoard();
		mongoClient.insert(DBConstants.T_BOARD, board.getDbObject());
		boardList.add(board);
		log.info("insert board = " + board.getDbObject());

		return boardList;
	}

	private static int FIND_BOARD_LIMIT = 10000;

	public static List<Board> getBoardList(MongoDBClient mongoClient,
			String appId, String gameId, int deviceType) {

		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_STATUS, Board.BoardStatusRun);
		query.put(DBConstants.F_DEVICE_TYPE, deviceType);
		// query.put(DBConstants.F_GAMEID, gameId);

		DBCursor cursor = mongoClient.find(DBConstants.T_BOARD, query, null, 0,
				FIND_BOARD_LIMIT);
		List<Board> list = null;
		if (cursor != null) {
			list = new ArrayList<Board>();
			while (cursor.hasNext()) {
				Board board = new Board(cursor.next());
				list.add(board);
			}
			cursor.close();
		}
		if (list != null && !list.isEmpty()) {
			log.info("GetBoardList query = " + query + "list count = "
					+ list.size());

			HashMap<String, Board> boardMap = new HashMap<String, Board>();
			for (Board board : list) {
				String boardID = board.getBoardId();
				// log.info("boardID = "+boardID);
				boardMap.put(boardID, board);
			}

			DBObject gameBoardQuery = new BasicDBObject();
			gameBoardQuery.put(DBConstants.F_GAMEID, gameId);
			DBObject inQuery = new BasicDBObject();
			inQuery.put("$in", boardMap.keySet());
			gameBoardQuery.put(DBConstants.F_BOARD_ID, inQuery);

			cursor = mongoClient.find(DBConstants.T_GAME_BOARD, gameBoardQuery,
					null, 0, FIND_BOARD_LIMIT);

			if (cursor == null) {
				return null;
			}
			List<Board> boardList = new ArrayList<Board>();
			while (cursor.hasNext()) {
				DBObject object = cursor.next();
				Object obj = object.get(DBConstants.F_INDEX);
				if (obj != null && (obj instanceof Number)) {
					int index = ((Number) obj).intValue();
					Board board = boardMap.get(object
							.get(DBConstants.F_BOARD_ID));
					board.setIndex(index);
					boardList.add(board);
				}
			}
			cursor.close();
			return boardList;
		}
		return null;
		// return getTestBoardList(mongoClient, appId);
	}

	public static void updateBoardStatistics(MongoDBClient mongoClient,
			String boardId, String userId, String appId, String gameId,
			String sourceIP, int deviceType) {
		//add a new click record
		DBObject object = new BasicDBObject();
		object.put(DBConstants.F_BOARD_ID, boardId);
		object.put(DBConstants.F_CREATE_USERID, userId);
		object.put(DBConstants.F_APPID, appId);
		object.put(DBConstants.F_GAMEID, gameId);
		object.put(DBConstants.F_SOURCE_IP, sourceIP);
		object.put(DBConstants.F_DEVICE_TYPE, deviceType);
		object.put(DBConstants.F_CREATE_DATE, new Date());
		
		mongoClient.insert(DBConstants.T_BOARD_RECORD, object);

		//update the click times
		BasicDBObject inc = new BasicDBObject();
		inc.put(DBConstants.F_CLICK_TIMES, 1);
		DBObject update = new BasicDBObject();
		update .put("$inc", inc);
		DBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(boardId));
		mongoClient.updateOne(DBConstants.T_BOARD, query,  update);
	}

	public static Board findBoard(MongoDBClient mongoClient, String boardId) {
		if (boardId == null)
			return null;
		
		DBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(boardId));
		DBObject obj = mongoClient.findOne(DBConstants.T_BOARD, query);
		if (obj == null)
			return null;
		
		return new Board(obj);
	}

    public static BBSBoard createBoard(String boardName, int boardSeq) {

        String gameId = DBConstants.GAME_ID_DRAW;
        ObjectId parentId = new ObjectId(DBConstants.TOP_BOARD_ID_DRAW);
        String defaultIcon = DBConstants.DEFAULT_BOARD_ICON;

        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_NAME, boardName);
        obj.put(DBConstants.F_INDEX, boardSeq);
        obj.put(DBConstants.F_CREATE_DATE, new Date());
        obj.put(DBConstants.F_ICON, defaultIcon);
        obj.put(DBConstants.F_PARENT_BOARDID, parentId);
        obj.put(DBConstants.F_GAMEID, gameId);
        obj.put(DBConstants.F_STATUS, 0);
        obj.put(DBConstants.F_TYPE, BBSBoard.BBSBoardTypeSub);

        mongoClient.insert(DBConstants.T_BBS_BOARD, obj);
        log.info("<createBoard> board="+obj.toString());

        BBSBoard board = new BBSBoard(obj);
        return board;

    }

    public static void deleteBoard(String boardId) {
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_ID, new ObjectId(boardId));

        BasicDBObject update = new BasicDBObject();
        update.put("$set", new BasicDBObject(DBConstants.F_GAMEID, DBConstants.C_DELETED_VALUE));

        DBObject obj = mongoClient.findAndModify(DBConstants.T_BBS_BOARD, query, update);
        log.info("<deleteBoard> boardId="+boardId+", after delete="+obj);
    }

    public static void updateBoard(String boardId, String boardName, int boardSeq) {
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_ID, new ObjectId(boardId));

        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();

        if (!StringUtil.isEmpty(boardName)){
            updateValue.put(DBConstants.F_NAME, boardName);
        }

        if (boardSeq != 0){
            updateValue.put(DBConstants.F_INDEX, boardSeq);
        }

        if (updateValue.size() == 0){
            log.info("<updateBoard> boardId="+boardId+", but nothing to update!");
            return;
        }

        update.put("$set", updateValue);

        DBObject obj = mongoClient.findAndModify(DBConstants.T_BBS_BOARD, query, update);
        log.info("<updateBoard> boardId="+boardId+", after update="+obj);

    }
}
