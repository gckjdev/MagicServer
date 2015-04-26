package com.orange.game.model.manager.song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.orange.game.api.service.ElasticsearchService;
import com.orange.game.model.common.MongoGetIdListUtils;
import com.orange.game.model.dao.User;
import org.bson.types.ObjectId;


import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.song.Song;
import com.orange.game.model.dao.song.SongCategory;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.service.DBService;

public class SongManager extends CommonManager {
	
    private static SongManager songManager = new SongManager();
    private static final MongoDBClient mongoClient = DBService.getInstance().getMongoDBClient();
    MongoGetIdListUtils<Song> idListUtils = new MongoGetIdListUtils<Song>();

    public static class SongObjectIdMap {
    	
    	private final String songName;
    	private final String objectID;
    	
		public SongObjectIdMap(String songName, String objectID) {
			this.songName = songName;
			this.objectID = objectID;
		}
		
		public String getSongName() {
			return songName;
		}

		public String getObjectID() {
			return objectID;
		}
    }

    public static SongManager getInstance() {
        return songManager;
    }

    private SongManager() {
    }
    
    
    /**
     *   往song表中插入一个新文档
     */
    public void writeOneSongIntoDB(Song song) {
    	
    	if (song == null) {
    		ServerLog.info(0, "<SongManager> song is null !");
    		return;
    	}
    	
    	// upsert, 有则不操作，无则插入
    	DBObject query = new BasicDBObject(DBConstants.F_SONG_ID, song.getSongID());
    	DBObject update = new BasicDBObject("$set", song.getDbObject());
    	mongoClient.updateOrInsert(DBConstants.T_SONG, query, update);
    }

    
    /**
     *  根据song表中的category字段,　插入新的分类信息:　{categor: subcategory}
     */
	public void updateCategoryInfoForSong(Song song, String category, String subcategory) {

		if (category == null) 
			category = "";

		DBObject query = new BasicDBObject(DBConstants.F_ID, song.getObjectId());
		
		// $addToSet, 有则不操作，无则插入
		DBObject categoryItem = new BasicDBObject(DBConstants.F_SONG_CATEGORY, new BasicDBObject(category, subcategory));
		DBObject update = new BasicDBObject("$addToSet", categoryItem);
		
		mongoClient.updateOrInsert(DBConstants.T_SONG, query, update);
	}

    
    
    /**　
     * 为某一歌手写入song_index表,　该表是该歌手的所有歌曲索引表,　F_SONG_OID_MAP是该表中
     *　一个数组,　保存所有歌名到song表中该歌的_id字段的映射
     */
	public void writeSingerSongIndexCollection(String singerName,List<SongObjectIdMap> allSongsObjectIds) {

		if (allSongsObjectIds == null || allSongsObjectIds.size() == 0)
			return ;
		
		// 构建DBObject对象
		DBObject dbObject = new BasicDBObject();
		dbObject.put(DBConstants.F_SONG_SINGER, singerName);
		
		BasicDBList songObjectIdArray = new BasicDBList();
		for (SongObjectIdMap so: allSongsObjectIds) {
			DBObject soObject = new BasicDBObject();
			soObject.put(so.getSongName(), so.getObjectID());
			songObjectIdArray.add(soObject);
		}
		dbObject.put(DBConstants.F_SONG_OID_MAP, songObjectIdArray);

		// upsert, 有则不操作，无则插入
		DBObject query = new BasicDBObject(DBConstants.F_SONG_SINGER, singerName);
    	DBObject update = new BasicDBObject("$set", dbObject);
		mongoClient.updateOrInsert(DBConstants.T_SINGER_SONG_INDEX, query, update);
	}

	/**
	 * 　往singer表中插入一个新文档
	 */
	public ObjectId writeSingerCollection(String nameCapital, String singerName, ObjectId singerSongIndexOId) {

		// 构建DBObject对象
		DBObject dbObject = new BasicDBObject();
		
		dbObject.put(DBConstants.F_SINGER_NAME_CAPITAL, nameCapital);
		dbObject.put(DBConstants.F_SONG_SINGER, singerName);
		dbObject.put(DBConstants.F_SINGER_SONG_INDEX_ID, singerSongIndexOId.toString());
		
		// upsert, 有则不操作，无则插入
		DBObject query = new BasicDBObject(DBConstants.F_SONG_SINGER, singerName);
		query.put(DBConstants.F_SINGER_SONG_INDEX_ID, singerSongIndexOId.toString());
    	DBObject update = new BasicDBObject("$set", dbObject);
		mongoClient.updateOrInsert(DBConstants.T_SINGER, query, update);
		
		return (ObjectId)dbObject.get(DBConstants.F_ID);
	}

	/**
	 *  根据歌名和歌手名字查找所有满足的song表中的对象
	 */
	public List<Song> findSongBySongNameAndSinger(String songName, String singerName) {

		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_SONG_NAME, songName);
		query.put(DBConstants.F_SONG_SINGER, singerName);
		
		DBObject orderBy = null;
		DBObject returnField = new BasicDBObject("_id", 1) ;
		
		int offset = 0;
		int limit = 0; // 不限 
		
		DBCursor cursor = mongoClient.find(DBConstants.T_SONG, query, returnField, orderBy, offset, limit);
		if (cursor != null) {
			List<Song> retList = new ArrayList<Song>();
			while (cursor.hasNext()) {
				DBObject object = cursor.next();
				if (object != null) {
					Song song = new Song(object);
					retList.add(song);
				}
			}
			cursor.close();
			return retList;
		}
		return Collections.emptyList();
	}

    /**
     * 根据歌手名找其singer_song_index表,返回该表ObjectId 
     */
	public ObjectId findSingerSongIndexOId(String singerName) {
		
		DBObject query = new BasicDBObject(DBConstants.F_SONG_SINGER, singerName);
		DBObject returnField = new BasicDBObject(DBConstants.F_ID, 1) ;
		DBObject returnDbObject = mongoClient.findOne(DBConstants.T_SINGER_SONG_INDEX, query, returnField);
		
		return (ObjectId) returnDbObject.get(DBConstants.F_ID);
	}


    public List<Song> searchSongs(String keyword, int offset, int limit) {

        List<User> result = new ArrayList<User>();

        // 在ES中进行多字段查找,目前查找的字段包括:
        // song_name, song_album, song_author
        List<String> candidateFields = new ArrayList<String>();
        candidateFields.add(DBConstants.ES_SONG_NAME);
        candidateFields.add(DBConstants.ES_SONG_AUTHOR);

        List<ObjectId> songIdList = ElasticsearchService.search(keyword, candidateFields,
                DBConstants.ES_SONG_ID, offset, limit, DBConstants.ES_INDEX_TYPE_SONG);

        return idListUtils.getList(mongoClient,  DBConstants.T_SONG, "_id", null, 0, songIdList, null, Song.class);
    }


    /**
	 *  服务接口,　根据category和subcategory,随机返回若干首歌曲.
	 *  如果两个参数为空字符串,　则范围定义在所有的歌曲中.
	 */
	public List<Song> randomGetSongs(MongoDBClient mongoClient, String category, String subcategory, int offset, int limit) {
		
		List<Song> result = new ArrayList<Song>();
		
		DBObject query;
		DBCursor cursor = null;
		
		// 主,次分类均未设的情况,　范围为所有歌曲
		if ( (category == null || category.equals(""))  && 
				(subcategory == null || subcategory.equals("")) ) 
		{
			double rand = Math.random();
	        query = new BasicDBObject(DBConstants.F_SONG_RANDOM, new BasicDBObject("$gt", rand));
			cursor = mongoClient.find(DBConstants.T_SONG, query, null, offset, limit);
			if ( cursor == null || cursor.count() == 0) {
				query = new BasicDBObject(DBConstants.F_SONG_RANDOM, new BasicDBObject("$lt", rand));
				cursor = mongoClient.find(DBConstants.T_SONG, query, null, offset, limit);
			}
			
			if (cursor != null) {
				while (cursor.hasNext()) {
					DBObject obj = cursor.next();
					if (obj != null) {
						Song song = new Song(obj);
						result.add(song);
					}
				}
				cursor.close();
			}
			return result;
		}
		
		// 主分类或次分类未设的情况
		query = new BasicDBObject();
		if (category != null && !category.equals("")) {
			query.put(DBConstants.F_SONG_CATEGORY, category);
		}
		if (subcategory != null && !subcategory.equals("")) {
			query.put(DBConstants.F_SONG_SUBCATEGORY, subcategory);
		}
		
		//　从song_category表中找出满足条件的文档,并从中抽取邮songsData数组,
		//　该数组存有[歌名:　歌曲在song表中oid]的映射, 抽取出oid集合,放入songOids列表 
		//  然后根据offset和limit随机生成下标，从songOids列表中取出元素
		cursor = mongoClient.find(DBConstants.T_SONG_CATEGORY, query, null, 0, 0);
		if (cursor != null && cursor.hasNext()) {
			DBObject obj = cursor.next();
			cursor.close();
			if (obj != null) {
				List<String> songOids = new SongCategory(obj).getSongOidsList();
				
				int size =  songOids.size();
				offset = offset % size;
				limit = (offset + limit <= size ? limit : size - offset);

				// 生成一个随机下标数组
				List<Integer> indexes = new ArrayList<Integer>(size);;
				for (int i = 0; i < size; i++) {
					indexes.add(i, i);  
				}
				Collections.shuffle(indexes);
				
				for (int k = 0; k < limit; k++) {
					// 根据生成的随机下标数组元素，取出对象以返回
					int index = indexes.get(k);
					String oid = songOids.get(index).replace("\"", "");
					
					Song song = new Song(mongoClient.findOne(DBConstants.T_SONG, new BasicDBObject(DBConstants.F_ID, new ObjectId(oid))));
					result.add(song);
				}
			}
		}
		
		return result;
	}

}