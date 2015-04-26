package com.orange.game.model.manager.config;

import com.orange.common.utils.PropertyUtil;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-23
 * Time: 下午1:46
 * To change this template use File | Settings | File Templates.
 */
public class ConfigManager {
    private static ConfigManager ourInstance = new ConfigManager();
    private String serverId = PropertyUtil.getStringProperty("server.server_id", "UNKNOWN_SERVER_ID");

    public static ConfigManager getInstance() {
        return ourInstance;
    }

    private ConfigManager() {
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
