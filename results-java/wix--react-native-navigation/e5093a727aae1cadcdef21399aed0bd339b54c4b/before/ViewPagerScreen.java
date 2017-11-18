package com.reactnativenavigation.screens;

import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.reactnativenavigation.params.ScreenParams;
import com.reactnativenavigation.params.TopTabParams;
import com.reactnativenavigation.views.ContentView;
import com.reactnativenavigation.views.TitleBarBackButtonListener;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ViewPagerScreen extends Screen {

    private List<ContentView> contentViews;
    private ViewPager viewPager;

    public ViewPagerScreen(AppCompatActivity activity, ScreenParams screenParams, TitleBarBackButtonListener backButtonListener) {
        super(activity, screenParams, backButtonListener);
    }

    @Override
    protected void createContent() {
        TabLayout tabLayout = topBar.initTabs();
        contentViews = new ArrayList<>();
        viewPager = new ViewPager(getContext());
        viewPager.setOffscreenPageLimit(99);
        RelativeLayout.LayoutParams lp = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        if (screenParams.styleParams.drawScreenBelowTopBar) {
            lp.addRule(BELOW, topBar.getId());
        }
        addView(viewPager, lp);

        for (TopTabParams topTabParam : screenParams.topTabParams) {
            ContentView contentView = new ContentView(getContext(),
                    topTabParam.screenId,
                    screenParams.passProps,
                    screenParams.navigationParams);
            addContent(contentView);
            contentViews.add(contentView);
        }

        ContentViewPagerAdapter adapter =
                new ContentViewPagerAdapter(viewPager, contentViews, screenParams.topTabParams);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void addContent(ContentView contentView) {
        LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        viewPager.addView(contentView, params);
    }

    @Override
    public void ensureUnmountOnDetachedFromWindow() {
        for (ContentView contentView : contentViews) {
            contentView.ensureUnmountOnDetachedFromWindow();
        }
    }

    @Override
    public void preventUnmountOnDetachedFromWindow() {
        for (ContentView contentView : contentViews) {
            contentView.preventUnmountOnDetachedFromWindow();
        }
    }

    @Override
    public void preventMountAfterReattachedToWindow() {
        for (ContentView contentView : contentViews) {
            contentView.preventMountAfterReattachedToWindow();
        }
    }

    public class ContentViewPagerAdapter extends PagerAdapter implements TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {

        private ViewPager viewPager;
        private List<ContentView> contentViews;
        private List<TopTabParams> topTabParams;

        public ContentViewPagerAdapter(ViewPager viewPager, List<ContentView> contentViews, List<TopTabParams> topTabParams) {
            this.viewPager = viewPager;
            this.contentViews = contentViews;
            this.topTabParams = topTabParams;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return contentViews.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {

        }

        @Override
        public int getCount() {
            return contentViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return topTabParams.get(position).title;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int position = tab.getPosition();
            viewPager.setCurrentItem(position);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}