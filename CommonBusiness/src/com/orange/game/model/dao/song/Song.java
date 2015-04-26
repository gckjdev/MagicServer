package com.orange.game.model.dao.song;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.elasticsearch.ESORMable;
import com.orange.common.utils.FileUtils;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.manager.utils.SongLyricDownloadManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Song extends CommonData implements ESORMable {

	private static final String BAIDU_SONG_URL_PREFIX = "http://music.baidu.com/song/";

    public Song(){
        super();
    }

	public Song(String songName, String songURL, String songAlbum,
			String singerName, String lyricPath) {
		
		super();
		setSongName(songName);
		setSongURL(songURL);
		setSongAlbum(songAlbum);
		setSingerName(singerName);
		setLyricPath(lyricPath);
		// 存入百度ID(数字),　以便于在处理分类时容易查找
		int baiduID = Integer.parseInt(getIDString(songURL));
		setSongID(baiduID);
		setRandomValue(Math.random());  // 随机数值,　用于RandomGetSongService
	}
	
	private String getIDString(String songURL) {
		
		String result = songURL.replace(BAIDU_SONG_URL_PREFIX, "");
		int indexOfHash = result.indexOf("#"); 
		if ( indexOfHash != -1) {
			result = result.substring(0, indexOfHash);
		}
		
		return result;
	}

	public Song(DBObject object) {
		super(object);
	}
	
	public void setSongName(String songName) {
		put(DBConstants.F_SONG_NAME, songName);
	}
	
	public String getSongName() {
		return getString(DBConstants.F_SONG_NAME);
	}
	
	public void setSongURL(String songURL) {
		put(DBConstants.F_SONG_URL, songURL);
	}

	public String getSongURL() {
		return getString(DBConstants.F_SONG_URL);
	}
	
	public void setSongAlbum(String songAlbum) {
		put(DBConstants.F_SONG_ALBUM, songAlbum);
    }

	public String getSongAlbum() {
		return getString(DBConstants.F_SONG_ALBUM);
	}
	
	public void setSingerName(String singerName) {
		put(DBConstants.F_SONG_SINGER, singerName);
	}

	public String getSingerName() {
		return getString(DBConstants.F_SONG_SINGER);
	}
	
	public void setLyricPath(String lyricPath) {
		put(DBConstants.F_SONG_LYRIC_PATH, lyricPath);
	}
	
	public String getLyricPath() {
		return getString(DBConstants.F_SONG_LYRIC_PATH);
	}
	
	public String getLyricURL() {
		String localPath = getLyricPath();
		if (localPath == null)
			return null;
		
		return SongLyricDownloadManager.getLyricURLPrefix() + localPath.replace("/data/songs", "");
	}

    public String getLyric() {
        String localPath = getLyricPath();
        if (localPath == null)
            return "";

        String lyric = FileUtils.stringFromFile(new File(localPath));
        if (lyric == null)
            return "";

        return lyric.trim();
    }


    public void setSongID(int songId) {
		put(DBConstants.F_SONG_ID, songId);
	}
	
	public int getSongID() {
		return getInt(DBConstants.F_SONG_ID);
	}
	
	public void setRandomValue(double random) {
		put(DBConstants.F_SONG_RANDOM, random);
	}
	
	public double getRandomValue() {
		return getDouble(DBConstants.F_SONG_RANDOM);
	}

	public String getAllTags() {
		
		StringBuilder result = new StringBuilder("");
		
		BasicDBList categories = (BasicDBList) getObject(DBConstants.F_SONG_CATEGORY);
		if (categories == null)
			return result.toString();
		
		for (Object o : categories.keySet()) {
			result.append("^^");
			
			String category = (String)o;
			String subcategory = categories.get(category).toString();
			
			result.append(category);
			result.append("$$");
			result.append(subcategory);
		}
		
		// 去除第一个"^^"
		return result.toString().replaceFirst("^^", "");
	}

    public String getSongTagsForES() {

        StringBuilder result = new StringBuilder("");

        BasicDBList categories = (BasicDBList) getObject(DBConstants.F_SONG_CATEGORY);
        if (categories == null)
            return result.toString();

        for (Object o : categories.keySet()) {
            result.append("^^");

            String category = (String)o;
            String subcategory = categories.get(category).toString();

            result.append(subcategory);
            result.append(" ");
        }

        return result.toString();
    }

    @Override
    public Map<String, Object> getESORM() {

        DBObject dbObject = getDbObject();
        String id = getStringObjectId();

        if (dbObject != null && !StringUtil.isEmpty(id)){

            Map<String, Object> userBean = new HashMap<String, Object>();

            userBean.put(DBConstants.ES_SONG_NAME, getSongName());
            userBean.put(DBConstants.ES_SONG_AUTHOR, getSingerName());
            userBean.put(DBConstants.ES_SONG_ALBUM, getSongAlbum());
            userBean.put(DBConstants.ES_SONG_TAGS, getSongTagsForES());
            userBean.put(DBConstants.ES_SONG_ID, id);

            return userBean;
        }

        return  null;
    }

    @Override
    public String getESIndexType() {
        return DBConstants.ES_INDEX_TYPE_SONG;
    }

    @Override
    public String getESIndexName() {
        return DBConstants.ES_INDEX_NAME;
    }

    @Override
    public String getID() {
        return getStringObjectId();
    }

    @Override
    public List<String> fieldsForIndex() {
        return null;
    }

    @Override
    public boolean hasFieldForSearch() {

        if (this.getDbObject() == null) return false;

        return (this.getDbObject().containsField(DBConstants.F_SONG_ALBUM)      ||
                this.getDbObject().containsField(DBConstants.F_SONG_NAME)   ||
                this.getDbObject().containsField(DBConstants.F_SONG_AUTHOR)
                );
    }

    @Override
    public boolean canBeIndexed() {
        return true;
    }
}