package com.orange.game.api.service.song;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.song.Song;
import com.orange.game.model.service.song.SongService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-10-22
 * Time: 下午12:52
 * To change this template use File | Settings | File Templates.
 */
public class SearchSongService extends CommonGameService {

    String keyword;
    int offset;
    int limit;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        keyword = request.getParameter(ServiceConstant.PARA_KEYWORD);
        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT, 30);

        if (StringUtil.isEmpty(keyword)){
            resultCode = ErrorCode.ERROR_PARAMETER_KEYWORD_EMPTY;
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {
        List<Song> songList = SongService.getInstance().searchSongs(keyword, offset, limit);
        byteData = CommonServiceUtils.songListToProtocolBuffer(songList);
        return;
    }
}
