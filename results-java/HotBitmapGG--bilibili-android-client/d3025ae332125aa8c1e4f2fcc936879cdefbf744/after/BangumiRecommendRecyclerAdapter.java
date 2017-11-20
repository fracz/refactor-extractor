package com.hotbitmapgg.ohmybilibili.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hotbitmapgg.ohmybilibili.R;
import com.hotbitmapgg.ohmybilibili.adapter.base.AbsRecyclerViewAdapter;
import com.hotbitmapgg.ohmybilibili.model.bangumi.BangumiRecommend;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hcc on 16/8/6 14:31
 * 100332338@qq.com
 * <p/>
 * 二次元新番Adapter
 */
public class BangumiRecommendRecyclerAdapter extends AbsRecyclerViewAdapter
{

    private List<BangumiRecommend.RecommendsBean> recommends = new ArrayList<>();

    public BangumiRecommendRecyclerAdapter(RecyclerView recyclerView, List<BangumiRecommend.RecommendsBean> recommends)
    {

        super(recyclerView);
        this.recommends = recommends;
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        bindContext(parent.getContext());
        return new ItemViewHolder(LayoutInflater.from(getContext())
                .inflate(R.layout.item_recommend_bangumi, parent, false));
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position)
    {

        if (holder instanceof ItemViewHolder)
        {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            BangumiRecommend.RecommendsBean recommendsBean = recommends.get(position);
            Picasso.with(getContext())
                    .load(recommendsBean.getPic())
                    .placeholder(R.drawable.bili_default_image_tv)
                    .into(itemViewHolder.mImage);

            itemViewHolder.mTitle.setText(recommendsBean.getTitle());
            itemViewHolder.mPlay.setText(recommendsBean.getPlay() + "人在看");
        }
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount()
    {

        return recommends.size();
    }

    private class ItemViewHolder extends AbsRecyclerViewAdapter.ClickableViewHolder
    {

        public ImageView mImage;

        public TextView mTitle;

        public TextView mPlay;

        public ItemViewHolder(View itemView)
        {

            super(itemView);
            mImage = $(R.id.item_img);
            mTitle = $(R.id.item_title);
            mPlay = $(R.id.item_play);
        }
    }
}