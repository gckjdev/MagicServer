package com.orange.game.model.manager.utils;

public class SongLyricDownloadManager {

	public static String getLyricURLPrefix() {
		String prefix = System.getProperty("lyric_url_prefix");
		if (prefix == null) 
			prefix = "";
		return prefix;
	}
	
}
