package com.orange.game.model.dao;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-7-30
 * Time: 下午4:01
 * To change this template use File | Settings | File Templates.
 */
public class UserFriend {

    private static final UserFriend NO_RELATION_FRIEND = new UserFriend(Relation.RELATION_TYPE_NO, null);
    final int relation;
    final String memo;

    public UserFriend(int relation, String memo){
       this.relation = relation;
       this.memo = memo;
    }

    public static UserFriend relationNO() {

        return NO_RELATION_FRIEND;
    }
}
