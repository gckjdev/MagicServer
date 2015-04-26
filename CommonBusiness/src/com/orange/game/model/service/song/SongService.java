package com.orange.game.model.service.song;

import com.orange.common.utils.StringUtil;

import com.orange.game.model.dao.song.Song;
import com.orange.game.model.manager.song.SongManager;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-16
 * Time: 下午1:01
 * To change this template use File | Settings | File Templates.
 */
public class SongService {
    private static SongService ourInstance = new SongService();

    public static SongService getInstance() {
        return ourInstance;
    }

    private SongService() {
    }

    public List<Song> searchSongs(String keyword, int offset, int limit) {
        if (StringUtil.isEmpty(keyword)){
            return Collections.emptyList();
        }

        return SongManager.getInstance().searchSongs(keyword, offset, limit);
    }
}
