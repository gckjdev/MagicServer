package com.orange.game.model.dao.group;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-2
 * Time: 下午3:55
 * To change this template use File | Settings | File Templates.
 */
public enum GroupRole {
    NONE(0),
    CREATOR(1),
    ADMIN(2),
    MEMBER(3),
    GUEST(4),    
//    REQUESTER(5),
//    INVITEE(6),
//    GUEST_INVITEE(7)
    ;
    public static final int INVALIDATE_VALUE = -1;


    private int permission;
    private int role;
    private String name;

    private int titleId;

    GroupRole(int role) {
        this.role = role;
        updatePermission(role);
    }

    public static GroupRole valueOf(int role){
        switch (role){
            case 1:
                return CREATOR;
            case 2:
                return ADMIN;
            case 3:
                return MEMBER;
            case 4:
                return GUEST;
            case 0:
           default:
                return NONE;
        }
    }


    private void updatePermission(int role) {
        switch (role){
            case 2:
                permission = GroupPermission.getAdminPermissionValue();
                name = "管理员";
                break;

            case 3:
                permission = GroupPermission.getUserPermissionValue();
                name = "成员";
                break;

            case 4:
                permission = GroupPermission.getGuestPermissionValue();
                name = "嘉宾";
            break;

            case 1:
                permission = GroupPermission.getCreatorPermissionValue();
                name = "创建者";
                break;

            case 0:
            default:
                permission = GroupPermission.getVisitorPermissionValue();
                name = "游客";
                break;
        }
    }

    public int getPermission() {
        return permission;
    }

    public String getName() {
        return name;
    }

    public int getRole() {
        return role;
    }

    public GroupTitle groupTitle()
    {
        GroupTitle title = new GroupTitle();
        title.setTitleId(getRole());
        title.setTitle(getName());

        return title;
    }


    public boolean isMember(){
        return isMember(role);
    }

    public boolean isGuest(){
        return this == GroupRole.GUEST;
    }

    public int getTitleId()
    {
        return titleId;
    }

    public void setTitleId(int titleId)
    {
        this.titleId = titleId;
    }

    public static boolean isMember(int role) {
        if (role == GroupRole.ADMIN.getRole())return true;
        if (role == GroupRole.CREATOR.getRole())return true;
        if (role == GroupRole.MEMBER.getRole())return true;
        return false;
    }

}
