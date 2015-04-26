package com.orange.barrage.model.user;

import com.orange.barrage.constant.BarrageConstants;

import java.util.Comparator;

/**
 * Created by pipi on 15/3/3.
 */
public class UserStatusUpdateComparator implements Comparator<User> {

    @Override
    public int compare(User user1, User user2) {
        int date1 = user1.getIntDate(BarrageConstants.F_STATUS_MODIFY_DATE);
        int date2 = user2.getIntDate(BarrageConstants.F_STATUS_MODIFY_DATE);
        return (date2 - date1);
    }
}
