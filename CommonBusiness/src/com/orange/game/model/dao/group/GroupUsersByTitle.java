package com.orange.game.model.dao.group;

import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.User;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.model.GroupProtos;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-2
 * Time: 上午11:04
 * To change this template use File | Settings | File Templates.
 */
public class GroupUsersByTitle implements ProtoBufCoding<GroupProtos.PBGroupUsersByTitle> {


    GroupTitle title;
    Set<User> users;

    public GroupUsersByTitle(GroupTitle title, Set<User> users) {
        super();
        this.title = title;
        this.users = users;
    }

    @Override
    public GroupProtos.PBGroupUsersByTitle toProtoBufModel() {

        GroupProtos.PBGroupUsersByTitle.Builder builder = GroupProtos.PBGroupUsersByTitle.newBuilder();
        if (title != null) {
            GroupProtos.PBGroupTitle pbTitle = title.toProtoBufModel();
            if (pbTitle != null) {
                builder.setTitle(pbTitle);
            }
        }
        if (users != null && !users.isEmpty()) {
            for (User user : users) {
                builder.addUsers(user.toPBUser());
            }
        }
        return builder.build();
    }

    @Override
    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {
        builder.addGroupMemberList(toProtoBufModel());
    }


}
