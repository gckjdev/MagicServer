package com.orange.barrage.model.feed.index;

import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.user.User;

import java.util.Comparator;

/**
 * Created by pipi on 15/3/24.
 */

public class MyNewFeedDateComparator implements Comparator<MyNewFeed> {

    @Override
    public int compare(MyNewFeed d1, MyNewFeed d2) {
        int date1 = d1.getInt(BarrageConstants.F_MODIFY_DATE);
        int date2 = d2.getInt(BarrageConstants.F_MODIFY_DATE);
        return (date2 - date1);
    }
}