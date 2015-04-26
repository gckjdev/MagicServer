package com.orange.barrage.model.chat;

import com.mongodb.BasicDBObject;
import com.orange.barrage.common.CommonModelManager;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.protocol.message.UserProtos;

import java.util.List;

/**
 * Created by pipi on 15/4/18.
 */
public class OnlineAgentManager extends CommonModelManager<Agent> {
    private static OnlineAgentManager ourInstance = new OnlineAgentManager();

    public static OnlineAgentManager getInstance() {
        return ourInstance;
    }

    private OnlineAgentManager() {

    }

    private int current = 0;

    public List<Agent> getOnlineAgents(){
        List<Agent> agents = findAll(BarrageConstants.F_AGENT_STATUS,
                Integer.valueOf(UserProtos.PBAgentStatus.AGENT_ONLINE_VALUE),
                getReturnFields());
        return agents;
    }

    private BasicDBObject getReturnFields() {
        BasicDBObject obj = new BasicDBObject();
        obj.put(BarrageConstants.F_NICK, 1);
        obj.put(BarrageConstants.F_USER_ID, 1);
        obj.put(BarrageConstants.F_LOCATION, 1);
        obj.put(BarrageConstants.F_AVATAR, 1);
        obj.put(BarrageConstants.F_SIGNATURE, 1);
        obj.put(BarrageConstants.F_AVATAR_BG, 1);
        obj.put(BarrageConstants.F_GENDER, 1);
        obj.put(BarrageConstants.F_STATUS_MODIFY_DATE, 1);
        obj.put(BarrageConstants.F_AGENT_STATUS, 1);
        obj.put(BarrageConstants.F_IS_AGENT, 1);
        obj.put(BarrageConstants.F_AGENT_ACCOUNT, 1);
        return obj;
    }


    public Agent assignAgent(Chat chat) {
        List<Agent> agents = getOnlineAgents();
        if (agents.size() == 0){
            return null;
        }

        Agent agent = agents.get(current % agents.size());
        current ++;
        return agent;
    }

    @Override
    public String getTableName() {
        return BarrageConstants.T_AGENT;
    }

    @Override
    public Class<Agent> getClazz() {
        return Agent.class;
    }

    public void initAgents(){

        UserProtos.PBUser.Builder builder = UserProtos.PBUser.newBuilder();
        builder.setAgentAccount("001");
        builder.setPassword("123456");
        builder.setAgentStatus(UserProtos.PBAgentStatus.AGENT_ONLINE_VALUE);
        builder.setGender(false);
        builder.setAvatar("http://img4.duitang.com/uploads/item/201404/29/20140429222927_isnwE.thumb.600_0.jpeg");
        builder.setLocation("广东 广州");
        builder.setUserId("");
        builder.setIsAgent(true);

        UserProtos.PBUser pbAgent = builder.build();

        BasicDBObject obj = Agent.pbToDBObject(pbAgent, true, BarrageConstants.F_USER_ID);
        mongoDBClient.insert(BarrageConstants.T_AGENT, obj);
    }
}
