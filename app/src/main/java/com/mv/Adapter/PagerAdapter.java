package com.mv.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


import com.mv.Fragment.CommunityHomeFragment;
import com.mv.Fragment.GroupsFragment;

import com.mv.Fragment.IndicatorListFragmet;
import com.mv.Fragment.ProgrammeManagmentFragment;
import com.mv.Fragment.TeamManagementFragment;
import com.mv.Fragment.TrainingFragment;

/**
 * Created by Rohit Gujar on 09-10-2017.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                CommunityHomeFragment tab4 = new CommunityHomeFragment();
                return tab4;
            case 1:
                GroupsFragment tab1 = new GroupsFragment();
                return tab1;
            case 2:
                ProgrammeManagmentFragment tab2 = new ProgrammeManagmentFragment();
                return tab2;
            case 3:
                TrainingFragment tab3 = new TrainingFragment();
                return tab3;
            case 4:
                IndicatorListFragmet tab5= new IndicatorListFragmet();
                return tab5;
            case 5:
                TeamManagementFragment tab6=new TeamManagementFragment();
                return tab6;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
