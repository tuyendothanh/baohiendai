package com.manuelmaly.hn.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.manuelmaly.hn.CategoriesFragment;
import com.manuelmaly.hn.R;
import com.manuelmaly.hn.model.CategoryListModel;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by dothanhtuyen on 2017/06/25.
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{

    private Context mContext;
    private List<CategoryListModel.CategoryData> mList;
    private LayoutInflater inflater;

    public CategoryAdapter(Context mContext,List<CategoryListModel.CategoryData> mList) {
        this.mContext = mContext;
        this.mList = mList;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ViewGroup.LayoutParams lp = holder.ivBg.getLayoutParams();

        final float scale = mContext.getResources().getDisplayMetrics().density;
        int dp2pxw = (int) (12 * scale + 0.5f);
        int dp2pxh = (int) (120 * scale + 0.5f);
        WindowManager windowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(dm);
        float DIMEN_RATE = dm.density / 1.0F;
        int DIMEN_DPI = dm.densityDpi;
        int SCREEN_WIDTH = dm.widthPixels;
        int SCREEN_HEIGHT = dm.heightPixels;
        if(SCREEN_WIDTH > SCREEN_HEIGHT) {
            int t = SCREEN_HEIGHT;
            SCREEN_HEIGHT = SCREEN_WIDTH;
            SCREEN_WIDTH = t;
        }
        lp.width = (SCREEN_WIDTH - dp2pxw) / 2;
        lp.height = dp2pxh;
        //lp.width = (App.SCREEN_WIDTH - SystemUtil.dp2px(mContext,12)) / 2;
        //lp.height = SystemUtil.dp2px(mContext,120);

        //ImageLoader.load(mContext,mList.get(position).getThumbnail(),holder.ivBg);
        Picasso.with(mContext).load(mList.get(position).getThumbnail()).into(holder.ivBg);
        holder.tvKind.setText(mList.get(position).getName());
        holder.tvDes.setText(mList.get(position).getDescription());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(mContext, CategoriesFragment.class);
                intent.putExtra(CategoriesFragment.IT_CATEGORY_ID, mList.get(holder.getAdapterPosition()).getId());
                intent.putExtra(CategoriesFragment.IT_CATEGORY_TITLE, mList.get(holder.getAdapterPosition()).getName());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBg;
        TextView tvKind;
        TextView tvDes;

        public ViewHolder(View itemView) {
            super(itemView);
            ivBg = (ImageView) itemView.findViewById(R.id.section_bg);
            tvKind = (TextView) itemView.findViewById(R.id.section_kind);
            tvDes = (TextView) itemView.findViewById(R.id.section_des);
        }
    }
}