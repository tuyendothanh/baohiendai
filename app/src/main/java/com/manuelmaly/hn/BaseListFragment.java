package com.manuelmaly.hn;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manuelmaly.hn.util.FontHelper;

import org.androidannotations.annotations.EFragment;

@EFragment
public class BaseListFragment extends Fragment {

    private TextView mLoadingView;

    protected TextView getEmptyTextView(ViewGroup parent) {
        if(mLoadingView == null) {
            ViewGroup root = (ViewGroup) getActivity().getLayoutInflater().
                    inflate(R.layout.panel_loading, parent, true);
            mLoadingView = (TextView) root.findViewById(android.R.id.empty);
            mLoadingView.setVisibility(View.GONE);
            mLoadingView.setTypeface(FontHelper.getComfortaa(getActivity(), true));
        }
        return mLoadingView;
    }
}
