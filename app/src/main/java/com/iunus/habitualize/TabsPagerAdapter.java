package com.iunus.habitualize;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Iunus on 08/02/2017.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter {
    final int TAB_COUNT = 2;
    private String[] tabTitles = new String[] {"Tasks", "Habits"};

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0 :
                return TasksFragment.newInstance();
            case 1 :
                return HabitsFragment.newInstance();
        }

        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }
}
