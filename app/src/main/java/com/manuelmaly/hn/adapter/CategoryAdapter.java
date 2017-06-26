package com.manuelmaly.hn.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manuelmaly.hn.App;
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
        //ViewGroup.LayoutParams lp = holder.ivBg.getLayoutParams();
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