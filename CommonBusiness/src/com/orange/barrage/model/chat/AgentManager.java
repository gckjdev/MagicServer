package com.orange.barrage.model.chat;

import com.orange.barrage.common.CommonModelManager;
import com.orange.barrage.constant.BarrageConstants;

/**
 * Created by pipi on 15/4/23.
 */
public class AgentManager extends CommonModelManager<Agent> {
    private static AgentManager ourInstance = new AgentManager();

    public static AgentManager getInstance() {
        return ourInstance;
    }

    private AgentManager() {
    }

    @Override
    public String getTableName() {
        return BarrageConstants.T_AGENT;
    }

    @Override
    public Class<Agent> getClazz() {
        return Agent.class;
    }
}
