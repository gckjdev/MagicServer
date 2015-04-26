package com.orange.game.model.dao.opus;

import java.util.Date;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.utils.IntegerUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.ScoreManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.network.game.protocol.model.DrawProtos;
import com.orange.network.game.protocol.model.OpusProtos.PBOpus;
import com.orange.network.game.protocol.model.SingProtos;
import org.bson.types.ObjectId;

public class Opus extends UserAction {

    public Opus() {
        super();
    }
	
	public Opus(String userId, PBOpus pbOpus, String localDataUrl, int dataLen,
			String localImageUrl, String localThumbImageUrl, AbstractXiaoji xiaoji) {
		
		super(new BasicDBObject());

        setActionId(new ObjectId());

        // TODO move to method
		put(DBConstants.F_DATA_URL, localDataUrl);
		put(DBConstants.F_DATA_LEN, dataLen);
		put(DBConstants.F_IMAGE_URL, localImageUrl);
		put(DBConstants.F_THUMB_URL, localThumbImageUrl);
		
		setType(pbOpus.getType().getNumber());
		setName(pbOpus.getName());
        setWord(pbOpus.getName());
		setDescription(pbOpus.getDesc());
		setCreateDate(new Date());
		setOpusStatus(STATUS_NORMAL);
		setDeviceType(pbOpus.getDeviceType());
		setDeviceModel(pbOpus.getDeviceName());
		setAppId(pbOpus.getAppId());
		
		setLanguage(pbOpus.getLanguage().getNumber());
		setCategory(pbOpus.getCategory().getNumber());

        setCreateDate(new Date());
        setTags(pbOpus.getTagsList());
        setSpendTime(pbOpus.getSpendTime());

		// set user infomation
		if (pbOpus.getAuthor() != null && !StringUtil.isEmpty(pbOpus.getAuthor().getUserId())){
			// TODO better structure
			setCreateUserId(pbOpus.getAuthor().getUserId());
			setNickName(pbOpus.getAuthor().getNickName());
			setSignature(pbOpus.getAuthor().getSignature());
			setAvatar(pbOpus.getAuthor().getAvatar());
			setGender(User.genderFromBool(pbOpus.getAuthor().getGender()));
		}
        else{
            User user = UserManager.findPublicUserInfoByUserId(userId);
            if (user != null){
                setCreateUserId(user.getUserId());
                setNickName(user.getNickName());
                setSignature(user.getSignature());
                setAvatar(user.getAvatar());
                setGender(user.getGender());
            }
        }
		
		if (pbOpus.getTargetUser() != null && !StringUtil.isEmpty(pbOpus.getTargetUser().getUserId())){
			// TODO better structure
			setTargetUserId(pbOpus.getTargetUser().getUserId());
			setTargetNickName(pbOpus.getTargetUser().getNickName());
		}

        // store label info
        if (pbOpus.getDescLabelInfo() != null){
            DrawProtos.PBLabelInfo labelInfo = pbOpus.getDescLabelInfo();
            setDescTextColor(labelInfo.getTextColor());
            setDescStrokeTextColor(labelInfo.getTextStrokeColor());
            setDescTextFontSize(labelInfo.getTextFont());
            setDescStyle(labelInfo.getStyle());
            if (labelInfo.getFrame() != null){
                setDescFrameX(labelInfo.getFrame().getX());
                setDescFrameY(labelInfo.getFrame().getY());
                setDescFrameWidth(labelInfo.getFrame().getWidth());
                setDescFrameHeight(labelInfo.getFrame().getHeight());
            }
        }

        if (pbOpus.getCanvasSize() != null){
            setCanvasWidth(pbOpus.getCanvasSize().getWidth());
            setCanvasHeight(pbOpus.getCanvasSize().getHeight());
        }

        setMatchTimes(0);
        setCorrectTimes(0);
        setCommentTimes(0);
        setGuessTimes(0);
        setFlowerTimes(0);
        setSaveTimes(0);

        // set history score
        ScoreManager.calculateAndSetHistoryScore(this, xiaoji);

        if (!StringUtil.isEmpty(pbOpus.getContestId())){
		    setContestId(pbOpus.getContestId());

            // calcuate contest score
    		setContestScore(ScoreManager.calculateContestScore(this, null));   // no contest object required here
        }
        else{
            // calculate score
            ScoreManager.calculateAndSetScore(this, xiaoji);
        }

		// set draw/sing/askps specifiy data here
        xiaoji.setOpusInfo(this, pbOpus);
        ScoreManager.calculateScore(this);
	}

    private BasicDBObject getOrCreateDescLabelInfo(){
        BasicDBObject obj = (BasicDBObject)getObject(DBConstants.F_DESC_LABEL_INFO);
        if (obj == null){
            obj = new BasicDBObject();
            put(DBConstants.F_DESC_LABEL_INFO, obj);
        }

        return obj;
    }

    private void setDescYRatio(float yRatio) {
        BasicDBObject descLabelInfo = getOrCreateDescLabelInfo();
        descLabelInfo.put(DBConstants.F_Y_RATIO, yRatio);
    }

    private void setDescXRatio(float xRatio) {
        BasicDBObject descLabelInfo = getOrCreateDescLabelInfo();
        descLabelInfo.put(DBConstants.F_X_RATIO, xRatio);

    }

    private void setDescStyle(int style) {
        BasicDBObject descLabelInfo = getOrCreateDescLabelInfo();
        descLabelInfo.put(DBConstants.F_STYLE, style);
    }

    private void setDescStrokeTextColor(int color) {
        BasicDBObject descLabelInfo = getOrCreateDescLabelInfo();
        long textStrokeColor = IntegerUtil.getUnsignedInt(color);
        descLabelInfo.put(DBConstants.F_STROKE_TEXT_COLOR, textStrokeColor);
    }

    private void setDescTextColor(int color) {
        BasicDBObject descLabelInfo = getOrCreateDescLabelInfo();
        long textColor = IntegerUtil.getUnsignedInt(color);
        descLabelInfo.put(DBConstants.F_TEXT_COLOR, textColor);
    }

    private void setDescTextFontSize(float fontSize) {
        BasicDBObject descLabelInfo = getOrCreateDescLabelInfo();
        descLabelInfo.put(DBConstants.F_TEXT_FONT_SIZE, fontSize);
    }

    private void setDescFrameX(int value) {
        BasicDBObject descLabelInfo = getOrCreateDescLabelInfo();
        descLabelInfo.put(DBConstants.F_FRAME_X, value);
    }

    private void setDescFrameY(int value) {
        BasicDBObject descLabelInfo = getOrCreateDescLabelInfo();
        descLabelInfo.put(DBConstants.F_FRAME_Y, value);
    }

    private void setDescFrameWidth(int value) {
        BasicDBObject descLabelInfo = getOrCreateDescLabelInfo();
        descLabelInfo.put(DBConstants.F_FRAME_WIDTH, value);
    }

    private void setDescFrameHeight(int value) {
        BasicDBObject descLabelInfo = getOrCreateDescLabelInfo();
        descLabelInfo.put(DBConstants.F_FRAME_HEIGHT, value);
    }

    public void setName(String name) {
		put(DBConstants.F_NAME, name);
	}
	
	public String getName() {
		// for draw, need to get from name, if missing then get from word, to make compatibility
		String name = getString(DBConstants.F_NAME);
		if (name == null){
			return getWord();
		}
		
		return name;
	}

    @Override
    public String getOpusId(){
        return getActionId();
    }




}

