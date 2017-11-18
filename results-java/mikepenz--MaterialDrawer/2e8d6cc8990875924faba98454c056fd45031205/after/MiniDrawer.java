package com.mikepenz.materialdrawer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mikepenz.materialdrawer.adapter.BaseDrawerAdapter;
import com.mikepenz.materialdrawer.adapter.DrawerAdapter;
import com.mikepenz.materialdrawer.interfaces.ICrossfader;
import com.mikepenz.materialdrawer.model.MiniDrawerItem;
import com.mikepenz.materialdrawer.model.MiniProfileDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

/**
 * Created by mikepenz on 15.07.15.
 * Don't count this for real yet. it's just a quick try on creating a Gmail like panel
 */
public class MiniDrawer {
    private RecyclerView mRecyclerView;
    private DrawerAdapter mDrawerAdapter;

    private Drawer mDrawer;

    public MiniDrawer withDrawer(@NonNull Drawer drawer) {
        this.mDrawer = drawer;
        return this;
    }

    private AccountHeader mAccountHeader;

    public MiniDrawer withAccountHeader(@NonNull AccountHeader accountHeader) {
        this.mAccountHeader = accountHeader;
        return this;
    }

    private ICrossfader mCrossFader;

    public MiniDrawer withCrossFader(@NonNull ICrossfader crossFader) {
        this.mCrossFader = crossFader;
        return this;
    }

    public View build(Context ctx) {
        mRecyclerView = new RecyclerView(ctx);

        //set the itemAnimator
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //some style improvements on older devices
        mRecyclerView.setFadingEdgeLength(0);
        //set the drawing cache background to the same color as the slider to improve performance
        //mRecyclerView.setDrawingCacheBackgroundColor(UIUtils.getThemeColorFromAttrOrRes(mActivity, R.attr.material_drawer_background, R.color.material_drawer_background));
        mRecyclerView.setClipToPadding(false);
        //additional stuff
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        //adapter
        mDrawerAdapter = new DrawerAdapter();
        mRecyclerView.setAdapter(mDrawerAdapter);

        createItems();

        return mRecyclerView;
    }

    public void onProfileClick() {
        //crossfade if we are cross faded
        if (mCrossFader != null) {
            if (mCrossFader.isCrossfaded()) {
                mCrossFader.crossfade();
            }
        }

        //update the current profile
        if (mAccountHeader != null) {
            IProfile profile = mAccountHeader.getActiveProfile();
            if (profile instanceof ProfileDrawerItem) {
                mDrawerAdapter.setDrawerItem(0, new MiniProfileDrawerItem((ProfileDrawerItem) profile));
            }
        }
    }

    public void onItemClick(IDrawerItem selectedDrawerItem) {
        //crossfade if we are cross faded
        if (mCrossFader != null) {
            if (mCrossFader.isCrossfaded()) {
                mCrossFader.crossfade();
            }
        }

        //We only need to clear if the new item is selectable
        if (selectedDrawerItem.isSelectable()) {
            //get the identifier
            int identifier = selectedDrawerItem.getIdentifier();

            //update everything
            if (mDrawer != null) {
                for (IDrawerItem drawerItem : mDrawerAdapter.getDrawerItems()) {
                    drawerItem.withSetSelected(drawerItem.getIdentifier() == identifier);
                }
                mDrawerAdapter.notifyDataSetChanged();
            }
        }
    }

    public void createItems() {
        mDrawerAdapter.clearDrawerItems();

        if (mAccountHeader != null) {
            IProfile profile = mAccountHeader.getActiveProfile();
            if (profile instanceof ProfileDrawerItem) {
                mDrawerAdapter.addDrawerItem(new MiniProfileDrawerItem((ProfileDrawerItem) profile));
            }
        }

        if (mDrawer != null) {
            if (mDrawer.getDrawerItems() != null) {
                ArrayList<IDrawerItem> drawerItems = mDrawer.getDrawerItems();
                if (mDrawer.switchedDrawerContent()) {
                    drawerItems = mDrawer.getOriginalDrawerItems();
                }

                //migrate to miniDrawerItems
                for (IDrawerItem drawerItem : drawerItems) {
                    if (drawerItem instanceof PrimaryDrawerItem) {
                        mDrawerAdapter.addDrawerItem(new MiniDrawerItem((PrimaryDrawerItem) drawerItem));
                    }
                }
            }
        }

        //listener
        mDrawerAdapter.setOnClickListener(new BaseDrawerAdapter.OnClickListener() {
            @Override
            public void onClick(View v, int position, IDrawerItem item) {
                if (item instanceof MiniDrawerItem) {
                    if (mDrawerAdapter != null) {
                        mDrawer.setSelection(item, true);
                    }
                } else if (item instanceof MiniProfileDrawerItem) {
                    if (mAccountHeader != null) {
                        if (!mAccountHeader.isSelectionListShown()) {
                            mAccountHeader.toggleSelectionList(v.getContext());
                        }
                    }
                    if (mCrossFader != null) {
                        mCrossFader.crossfade();
                    }
                }
            }
        });
        mRecyclerView.scrollToPosition(0);
    }
}