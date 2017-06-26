package com.manuelmaly.hn;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by TPS on 6/23/2017.
 */

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 2;

    /** Constructor of the class */
    public MainFragmentPagerAdapter(FragmentManager fm) {
        super(fm);

    }

    /** This method will be invoked when a page is requested to create */
    @Override
    public Fragment getItem(int arg0) {
        Bundle data = new Bundle();
        switch(arg0){

            /** tab1 is selected */
            case 0:
                MainListFragment_ mainListFragment = new MainListFragment_();
                return mainListFragment;

            /** tab2 is selected */
            case 1:
                CategoriesFragment_ categoriesFragment = new CategoriesFragment_();
                return categoriesFragment;

        }

        return null;
    }

    /** Returns the number of pages */
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

}