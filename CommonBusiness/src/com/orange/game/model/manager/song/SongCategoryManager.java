package com.orange.game.model.manager.song;

import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.song.SongCategory;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.service.DBService;

public class SongCategoryManager extends CommonManager {
	
    private static SongCategoryManager ourInstance = new SongCategoryManager();
    private static final MongoDBClient mongoClient = DBService.getInstance().getMongoDBClient();

    public static SongCategoryManager getInstance() {
        return ourInstance;
    }

    private SongCategoryManager() {
    }


	/**
	 *  写入song_category表,　该表包含{categor: subcategory}分类下的所有歌曲信息
	 * 　歌曲信息格式为:{歌曲:　该歌曲在song表中的_id}
	 */
	public void writeSongCategoryCollection(SongCategory songCategory) {

		if (songCategory == null)
			return;
		
		mongoClient.insert(DBConstants.T_SONG_CATEGORY, songCategory.getDbObject());
	}

}