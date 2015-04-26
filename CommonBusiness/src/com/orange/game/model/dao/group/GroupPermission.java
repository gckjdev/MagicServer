package com.orange.game.model.dao.group;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-2
 * Time: 下午3:49
 * To change this template use File | Settings | File Templates.
 */
public enum GroupPermission {

    NONE(0),      //无权限

    //GROUP
    ACCESS_GROUP       (0x1 << 1),     //进入家族
    JOIN_GROUP         (0x1 << 2),     //申请加入家族
    QUIT_GROUP         (0x1 << 3),     //退出家族
    CHAT_GROUP         (0x1 << 4),     //群聊

    //TOPIC
    READ_TOPIC         (0x1 << 10),     //读贴
    CREATE_TOPIC       (0x1 << 11),     //发帖
    DELETE_TOPIC       (0x1 << 12),     //删帖
    REPLY_TOPIC        (0x1 << 13),     //回复
    MARK_TOPIC         (0x1 << 14),     //加精
    TOP_TOPIC          (0x1 << 15),     //置顶话题
    CREATE_PRIVATE_TOPIC(0x1 << 16),     //发私密话题
    READ_PRIVATE_TOPIC  (0x1 << 17),     //读私密话题

    //USER
    HANDLE_REQUEST     (0x1 << 20),     //通过或者拒绝加入请求
    INVITE_USER        (0x1 << 21),     // 邀请用户
    INVITE_GUEST       (0x1 << 22),     // 邀请嘉宾
    EXPEL_USER         (0x1 << 23),     //T人
    CUSTOM_TITLE       (0x1 << 24),     // 自定义title
    ARRANGE_ADMIN      (0x1 << 25),     //管理管理员
    ARRANGE_PERMISSION (0x1 << 26),     //分配权限
    UPGRADE_GROUP      (0x1 << 27),     //升级群
    DISMISSAL_GROUP    (0x1 << 28),     //解散群
    EDIT_GROUP         (0x1 << 30),     //编辑群

    HOLD_CONTEST       (0x1 << 31),     //举办比赛
;

//    Visitor

    private final static GroupPermission visitorPermissions[] = {
            ACCESS_GROUP,
            JOIN_GROUP,
            READ_TOPIC,
    };

    private final static GroupPermission guestPermissions[] = {
            ACCESS_GROUP,
//JOIN_GROUP,
            READ_TOPIC,
            CREATE_TOPIC,
            QUIT_GROUP,
            CHAT_GROUP,
            REPLY_TOPIC,
    };


    private final static GroupPermission userPermissions[] = {
            ACCESS_GROUP,
            READ_TOPIC,
            CREATE_TOPIC,
            REPLY_TOPIC,
            QUIT_GROUP,
            CHAT_GROUP,
            CREATE_PRIVATE_TOPIC,
            READ_PRIVATE_TOPIC
    };


    private final static GroupPermission adminPermissions[] = {
            ACCESS_GROUP,
            READ_TOPIC,
            CREATE_TOPIC,

            DELETE_TOPIC,
            REPLY_TOPIC,
            MARK_TOPIC,
            TOP_TOPIC,

            CREATE_PRIVATE_TOPIC,
            READ_PRIVATE_TOPIC,

            //USER
            CUSTOM_TITLE,
            INVITE_USER,
            HANDLE_REQUEST,
            ARRANGE_PERMISSION,
            EXPEL_USER,
            INVITE_GUEST,
            QUIT_GROUP,
            CHAT_GROUP,
            EDIT_GROUP,
            HOLD_CONTEST,
    };

    private final static GroupPermission creatorPermissions[] = {
            ACCESS_GROUP,
            READ_TOPIC,
            CREATE_TOPIC,

            DELETE_TOPIC,
            REPLY_TOPIC,
            MARK_TOPIC,
            TOP_TOPIC,

            CREATE_PRIVATE_TOPIC,
            READ_PRIVATE_TOPIC,

            //USER
            CUSTOM_TITLE,
            INVITE_USER,
            HANDLE_REQUEST,
            ARRANGE_PERMISSION,
            EXPEL_USER,
            INVITE_GUEST,
            // QUIT_GROUP,
            CHAT_GROUP,
            ARRANGE_ADMIN,
            UPGRADE_GROUP,
            DISMISSAL_GROUP,
            EDIT_GROUP,
            HOLD_CONTEST,
    };

    protected int permission;


    GroupPermission(int permission) {
        this.permission = permission;
    }

    private static int getPermissionValue(GroupPermission permissions[]){
        int value = NONE.getPermission();
        for (GroupPermission p : permissions) {
            value += p.getPermission();
        }
        return value;

    }

    public static int getUserPermissionValue() {
        return getPermissionValue(userPermissions);
    }

    public static int getAdminPermissionValue() {
        return getPermissionValue(adminPermissions);
    }

    public static int getGuestPermissionValue() {
        return getPermissionValue(guestPermissions);
    }

    public static int getVisitorPermissionValue() {
        return getPermissionValue(visitorPermissions);
    }

    public static int getCreatorPermissionValue() {
        return getPermissionValue(creatorPermissions);
    }

    public int getPermission() {
        return permission;
    }
}
