package com.orange.game.model.xiaoji;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.opus.Opus;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.ScoreManager;
import com.orange.game.model.manager.opus.LatestOpusManager;
import com.orange.game.model.manager.utils.ImageUploadManager;
import com.orange.game.model.service.DataService;
import com.orange.network.game.protocol.model.DrawProtos;
import com.orange.network.game.protocol.model.OpusProtos;
import com.orange.network.game.protocol.model.SingProtos;

public class XiaojiSing extends AbstractXiaoji {

	final LatestOpusManager nichangwocaiLatestOpusManager = new LatestOpusManager(getCategoryName(), DBConstants.C_SUB_CATEGORY_NICHANGWOCAI);
	
	@Override
	public String getCategoryName() {
		return "sing";
	}

	public int getCategoryType(){
		return DBConstants.C_CATEGORY_TYPE_SING;
	}

	@Override
	public LatestOpusManager latestOpusManager(String appId, int language) {
		return nichangwocaiLatestOpusManager;
	}
	
	@Override
	public boolean isZipUploadDataFile() {
		return false;
	}

    @Override
    public void setOpusInfo(Opus opus, OpusProtos.PBOpus pbOpus) {
        SingProtos.PBSingOpus pbSingOpus = pbOpus.getSing();
        if (pbSingOpus == null)
            return;

        if (pbSingOpus.getSong() != null){
            opus.setSongName(pbSingOpus.getSong().getName());
            opus.setSongAuthor(pbSingOpus.getSong().getAuthor());
            opus.setSongId(pbSingOpus.getSong().getSongId());
            opus.setSongLyricURL(pbSingOpus.getSong().getLyric());
        }
        opus.setSingVoiceType(pbSingOpus.getVoiceType().getNumber());
        opus.setSingDuration(pbSingOpus.getDuration());
        opus.setSingPitch(pbSingOpus.getPitch());
        opus.setSingFormant(pbSingOpus.getFormant());
    }

    @Override
    public void opusToPB(UserAction opus, OpusProtos.PBOpus.Builder builder){

        SingProtos.PBSingOpus.Builder singBuilder = SingProtos.PBSingOpus.newBuilder();

        if (opus.getSongId() != null && opus.getSongName() != null){
            SingProtos.PBSong.Builder songBuilder = SingProtos.PBSong.newBuilder();
            songBuilder.setSongId(opus.getSongId());
            songBuilder.setName(opus.getSongName());

            if (opus.getSongLyricURL() != null)
                songBuilder.setLyric(opus.getSongLyricURL());

            if (opus.getSongAuthor() != null)
                songBuilder.setAuthor(opus.getSongAuthor());

            singBuilder.setSong(songBuilder.build());
        }

        if (opus.getSingVoiceType() != null){
            singBuilder.setVoiceType(opus.getSingVoiceType());
        }
        singBuilder.setDuration(opus.getSingDuration());
        singBuilder.setPitch(opus.getSingPitch());
        singBuilder.setFormant(opus.getSingFormant());

        builder.setSing(singBuilder.build());
    }

    public static void opusToPB(UserAction opus, DrawProtos.PBFeed.Builder builder){
        SingProtos.PBSingOpus.Builder singBuilder = SingProtos.PBSingOpus.newBuilder();

        if (opus.getSongId() != null && opus.getSongName() != null){
            SingProtos.PBSong.Builder songBuilder = SingProtos.PBSong.newBuilder();
            songBuilder.setSongId(opus.getSongId());
            songBuilder.setName(opus.getSongName());

            if (opus.getSongLyricURL() != null)
                songBuilder.setLyric(opus.getSongLyricURL());

            if (opus.getSongAuthor() != null)
                songBuilder.setAuthor(opus.getSongAuthor());

            singBuilder.setSong(songBuilder.build());
        }

        if (opus.getSingVoiceType() != null){
            singBuilder.setVoiceType(opus.getSingVoiceType());
        }
        singBuilder.setDuration(opus.getSingDuration());
        singBuilder.setPitch(opus.getSingPitch());
        singBuilder.setFormant(opus.getSingFormant());

        builder.setSing(singBuilder.build());

    }

    @Override
    public double calculateAndSetHistoryScore(UserAction action) {
        return ScoreManager.calculateAndSetHistorySingScore(action);
    }

    @Override
    public double calculateHotScore(UserAction action) {
        return ScoreManager.calculateHotSingScore(action);
    }

    @Override
    public boolean isOneUserOneOpusForLatest() {
        return false;
    }


}
