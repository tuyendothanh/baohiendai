package com.manuelmaly.hn;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manuelmaly.hn.adapter.CategoryAdapter;
import com.manuelmaly.hn.model.CategoryListModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TPS on 6/23/2017.
 */
@EFragment(R.layout.categories_fragment)
public class CategoriesFragment extends Fragment {
    public static final String IT_CATEGORY_ID = "CATEGORY_ID";
    public static final String IT_CATEGORY_TITLE = "CATEGORY_TITLE";

    @ViewById(R.id.view_main)
    RecyclerView rvSectionList;
    @ViewById(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    List<CategoryListModel.CategoryData> mList;
    CategoryAdapter mAdapter;

    Activity mActivity;
    Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.categories_fragment, null);
    }
    @AfterViews
    public void init() {
        mActivity = getActivity();
        mContext = getActivity();
        mList = new ArrayList<>();
        mAdapter = new CategoryAdapter(mContext,mList);
        rvSectionList.setLayoutManager(new GridLayoutManager(mContext, 2));
        rvSectionList.setAdapter(mAdapter);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showContent(createDmy());
            }
        });

        showContent(createDmy());
    }

    public void showContent(CategoryListModel categoryListModel) {
        if(swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
        mList.clear();
        mList.addAll(categoryListModel.getData());
        mAdapter.notifyDataSetChanged();
    }

    private CategoryListModel createDmy() {
        CategoryListModel categoryListModel = new CategoryListModel();

        ArrayList<CategoryListModel.CategoryData> data = new ArrayList<CategoryListModel.CategoryData>();
        CategoryListModel.CategoryData categoryData = new CategoryListModel.CategoryData("description", "id", "name", "http://img.f31.vnecdn.net/2017/06/26/TELEMMGLPICT0001330151721largetransNvBQzQNjv4BqwioWl5aH7fAEJ8IWJw2Y5PHkRvugymKLtqq96rVP8-1498434042_140x84.jpeg");
        data.add(categoryData);
        CategoryListModel.CategoryData categoryData1 = new CategoryListModel.CategoryData("description", "id", "name", "http://img.f32.vnecdn.net/2017/06/23/Hyundai-Sonata-2014-13-3791-1498210628_140x84.jpg");
        data.add(categoryData1);
        categoryListModel.setData(data);

        return categoryListModel;
    }
}