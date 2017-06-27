package com.manuelmaly.hn.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.manuelmaly.hn.R;
import com.manuelmaly.hn.model.HNPost;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by dothanhtuyen on 2017/06/27.
 */

public class ArticleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<HNPost> mList;
    private LayoutInflater inflater;
    private Context mContext;
    private OnItemClickListener onItemClickListener;


    public ArticleAdapter(Context mContext, List<HNPost> mList) {
        this.mList = mList;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_list_item, null);
        RecyclerView.ViewHolder viewHolder = new ArticleViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ArticleViewHolder) {
            ((ArticleViewHolder)holder).title.setText(mList.get(position).getTitle());
            if (mList.get(position).getReadState()) {
                ((ArticleViewHolder)holder).title.setTextColor(ContextCompat.getColor(mContext,R.color.news_read));
            } else {
                ((ArticleViewHolder)holder).title.setTextColor(ContextCompat.getColor(mContext,R.color.news_unread));
            }
            //ImageLoader.load(mContext,mList.get(position).getImages().get(0),((ArticleViewHolder)holder).image);
            Picasso.with(mContext).load(mList.get(position).getSrc()).into(((ArticleViewHolder)holder).image);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemClickListener != null) {
                        ImageView iv = (ImageView) view.findViewById(R.id.iv_daily_item_image);
                        final int pos = position;
                        onItemClickListener.onItemClick(pos,iv);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        public ArticleViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_daily_item_title);
            image = (ImageView) itemView.findViewById(R.id.iv_daily_item_image);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position,View view);
    }
}
