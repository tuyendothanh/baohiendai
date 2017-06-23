package com.manuelmaly.hn;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by TPS on 6/23/2017.
 */

public class MainFragmentActivity extends AppCompatActivity {

    private ViewPager mPager;

    ActionBar mActionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        /** Getting a reference to action bar of this activity */
        mActionbar = getSupportActionBar();

        /** Set tab navigation mode */
        mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        /** Getting a reference to ViewPager from the layout */
        mPager = (ViewPager) findViewById(R.id.pager1);

        /** Getting a reference to FragmentManager */
        FragmentManager fm = getSupportFragmentManager();

        /** Defining a listener for pageChange */
        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mActionbar.setSelectedNavigationItem(position);
            }

        };

        /** Setting the pageChange listener to the viewPager */
        mPager.setOnPageChangeListener(pageChangeListener);

        /** Creating an instance of FragmentPagerAdapter */
        MainFragmentPagerAdapter fragmentPagerAdapter = new MainFragmentPagerAdapter(fm);

        /** Setting the FragmentPagerAdapter object to the viewPager object */
        mPager.setAdapter(fragmentPagerAdapter);

        mActionbar.setDisplayShowTitleEnabled(false);
        mActionbar.setDisplayUseLogoEnabled(false);
        mActionbar.setDisplayShowHomeEnabled(false);

        /** Defining tab listener */
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };

        /** Creating fragment1 Tab */
        ActionBar.Tab tab = mActionbar.newTab()
                .setText("Tab1")
                .setTabListener(tabListener);

        mActionbar.addTab(tab);

        /** Creating fragment2 Tab */
        tab = mActionbar.newTab()
                .setText("Tab2")
                .setTabListener(tabListener);

        mActionbar.addTab(tab);

    }
}