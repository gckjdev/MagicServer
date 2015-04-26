package com.orange.game.model.dao.common;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.model.GameBasicProtos;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-10
 * Time: 下午12:06
 * To change this template use File | Settings | File Templates.
 */
public class IntKeyValue {

    final int key;
    final String value;

    public IntKeyValue(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey(){
        return key;
    }

    public String getValue(){
        return value;
    }

    public GameBasicProtos.PBIntKeyValue toPBIntKeyValue() {
        if (this.value == null)
            return null;

        return GameBasicProtos.PBIntKeyValue.newBuilder().setKey(key).setValue(value).build();
    }
}
