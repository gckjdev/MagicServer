package com.orange.game.api.service.song;

import java.util.List;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.song.Song;
import com.orange.game.model.manager.song.SongManager;

import javax.servlet.http.HttpServletRequest;

public class RandomGetSongService extends CommonGameService {

    private String category;
    private String subcategory;
    private int offset;
	private int limit;
    
    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
    	
    	category = request.getParameter(ServiceConstant.PARA_SONG_CATEGORY);
    	subcategory = request.getParameter(ServiceConstant.PARA_SONG_SUBCATEGORY);
    	offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
 	    limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT, ServiceConstant.CONST_DEFAULT_PAGE_COUNT);
        return true;
    }

    @Override
    public void handleData() {
    	List<Song> songs = SongManager.getInstance().randomGetSongs(mongoClient, category, subcategory, offset, limit);
    	byteData = CommonServiceUtils.songListToProtocolBuffer(songs);
    }
}
