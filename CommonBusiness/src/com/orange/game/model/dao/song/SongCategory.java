package com.orange.game.model.dao.song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;

public class SongCategory extends CommonData {

	public SongCategory(DBObject object) {
		super(object);
	}
	
	public SongCategory(String category, String subcategory, Map<String, String> songsInThisCategory) {
		setCategory(category);
		setSubcategory(subcategory);
		setSongsData(songsInThisCategory);
	}

	public void setCategory(String category) {
		put(DBConstants.F_SONG_CATEGORY, category);
	}

	public String getCategory() {
		return getString(DBConstants.F_SONG_CATEGORY);
	}

	public void setSubcategory(String subcategory) {
		put(DBConstants.F_SONG_SUBCATEGORY, subcategory);
	}

	public String getSubcategory() {
		return getString(DBConstants.F_SONG_SUBCATEGORY);
	}

	public void setSongsData(Map<String, String> songsInThisCategory) {
		if (songsInThisCategory == null)
			return;

		BasicDBList dbList = new BasicDBList();
		for (Entry<String, String> entry: songsInThisCategory.entrySet()) {
			String songName = entry.getKey();
			String oid = entry.getValue();
			dbList.add(new BasicDBObject(songName, oid));
		}

		put(DBConstants.F_SONGS_DATA, dbList);
		
	}

	public Map<String, String> getSongsData() {
		Map<String, String> result = new HashMap<String, String>();

		BasicDBList songsDataList = (BasicDBList) getObject(DBConstants.F_SONGS_DATA);
		for ( Object o : songsDataList) {
			// 格式： { "歌名" : "oid"}
			String[] songData = o.toString().split(":");
			String songName = songData[0].replace("{", "").trim();
			String songOid = songData[1].replace("}", "").trim();
			result.put(songName, songOid);
		}

		return result;
	}
	
	public List<String> getSongNamesList() {
		
		List<String> result = new ArrayList<String>();
		
		Map<String, String> songsData = getSongsData();
		for (Entry<String, String> entry : songsData.entrySet()) {
			result.add(entry.getKey());
		}
		
		return result;
	}

    public List<String> getSongOidsList() {
		
		List<String> result = new ArrayList<String>();
		
		Map<String, String> songsData = getSongsData();
		
		for (Entry<String, String> entry : songsData.entrySet()) {
			result.add(entry.getValue());
		}

		return result;
	}
}