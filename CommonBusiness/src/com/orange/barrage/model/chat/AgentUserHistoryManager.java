package com.orange.barrage.model.chat;

import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.user.User;
import com.orange.common.utils.StringUtil;
import com.orange.game.model.common.CommonZSetIndexManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by pipi on 15/4/27.
 */
public class AgentUserHistoryManager extends CommonZSetIndexManager<User> {

    public AgentUserHistoryManager(String agentId) {
        super(REDIS_KEY+agentId, MONGO_TABLE_NAME, TOP_COUNT, User.class);
    }

    static Logger log = Logger.getLogger(AgentUserHistoryManager.class.getName());

    private static final String REDIS_KEY = "agent_user_history_";
    private static final String MONGO_TABLE_NAME = BarrageConstants.T_USER;
    private static final int TOP_COUNT = 0; // no limit

    public void addUserForAgent(final String userId, final double score){
        this.updateTopScore(userId, score, null, false, true);
    }

    public List<User> getUserList(int offset,int limit){
        return getTopList(offset, limit, null, 0, User.getPublicReturnFields());
    }

    // write agent user history
    public static void writeAgentUserHistory(String agentId, String userId){
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(agentId)){
            return;
        }

        AgentUserHistoryManager manager = new AgentUserHistoryManager(agentId);
        manager.addUserForAgent(userId, System.currentTimeMillis());
    }

    // get agent user history list
    public static List<User> getAgentUserHistory(String agentId, int offset,int limit){
        if (StringUtil.isEmpty(agentId)){
            return null;
        }

        AgentUserHistoryManager manager = new AgentUserHistoryManager(agentId);
        return manager.getTopList(offset, limit, null, 0, User.getPublicReturnFields());
    }
}
