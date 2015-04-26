package com.orange.barrage.model.user;

import com.mongodb.DBObject;
import com.orange.game.model.dao.CommonData;

/**
 * Created by pipi on 14/12/25.
 */
public class FriendRequest extends CommonData {

    public FriendRequest(DBObject dbObject) {
        super(dbObject);
    }

    public FriendRequest() {
        super();
    }

}
