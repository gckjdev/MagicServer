package com.orange.game.model.service.opus;

import com.orange.game.model.dao.opus.Opus;

public interface OpusNotificationInterface {

    public void notifyOpusUpdate(final String userId, final String opusId, final double opusScore);
    public void notifyOpusCreate(final String userId, final Opus opus);

    public boolean processNotificationAtBackground();
}
