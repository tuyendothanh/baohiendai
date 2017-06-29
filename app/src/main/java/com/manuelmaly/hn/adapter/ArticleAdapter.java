package com.manuelmaly.hn.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manuelmaly.hn.R;
import com.manuelmaly.hn.model.HNFeed;
import com.squareup.picasso.Picasso;

/**
 * Created by dothanhtuyen on 2017/06/27.
 */

public class ArticleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEWTYPE_POST = 0;
    private static final int VIEWTYPE_LOADMORE = 1;

    private HNFeed mFeed;
    private LayoutInflater inflater;
    private Context mContext;
    private OnItemClickListener onItemClickListener;


    public ArticleAdapter(Context mContext, HNFeed mFeed) {
        this.mFeed = mFeed;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getItemViewType(int position) {
        return VIEWTYPE_POST;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //return new ArticleViewHolder(inflater.inflate(R.layout.item_article, parent, false));
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, null);
        RecyclerView.ViewHolder viewHolder = new ArticleViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof ArticleViewHolder) {
            if (position < mFeed.getPosts().size() ) {
                ((ArticleViewHolder) holder).title.setText(mFeed.getPosts().get(position).getTitle());
                if (mFeed.getPosts().get(position).getReadState()) {
                    ((ArticleViewHolder) holder).title.setTextColor(ContextCompat.getColor(mContext, R.color.news_read));
                } else {
                    ((ArticleViewHolder) holder).title.setTextColor(ContextCompat.getColor(mContext, R.color.news_unread));
                }
                //ImageLoader.load(mContext,mList.get(position).getImages().get(0),((ArticleViewHolder)holder).image);
                if (!mFeed.getPosts().get(position).getSrc().isEmpty()) {
                    Picasso.with(mContext).load(mFeed.getPosts().get(position).getSrc()).into(((ArticleViewHolder) holder).image);
                }
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null) {
                    ImageView iv = (ImageView) view.findViewById(R.id.iv_daily_item_image);
                    if (position < mFeed.getPosts().size() ) {
                        onItemClickListener.onItemClick(position, iv);
                    }
                }
            }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mFeed.getPosts().size();
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

    public void setReadState(int position,boolean readState) {
        mFeed.getPosts().get(position).setReadState(readState);
    }
}
