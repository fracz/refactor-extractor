package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import io.github.izzyleung.zhihudailypurify.support.lib.MyAsyncTask;
import io.github.izzyleung.zhihudailypurify.task.AccelerateGetNewsTask;
import io.github.izzyleung.zhihudailypurify.task.BaseGetNewsTask;
import io.github.izzyleung.zhihudailypurify.task.OriginalGetNewsTask;
import io.github.izzyleung.zhihudailypurify.ui.widget.SwipeRefreshLayout;

public class NewsListFragment extends BaseNewsFragment
        implements SwipeRefreshLayout.OnRefreshListener, BaseGetNewsTask.UpdateUIListener {
    private String date;

    private boolean isAutoRefresh;
    private boolean isToday;

    // Fragment is single in PortalActivity
    private boolean isSingle;
    private boolean isRefreshed = false;

    private ListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        new RecoverNewsListTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            date = bundle.getString("date");
            isToday = bundle.getBoolean("first_page?");
            isSingle = bundle.getBoolean("single?");

            setRetainInstance(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);

        assert view != null;
        mListView = (ListView) view.findViewById(R.id.news_list);
        mListView.setAdapter(listAdapter);
        mListView.setOnScrollListener(
                new PauseOnScrollListener(ImageLoader.getInstance(), false, true, this));

        mListView.setOnItemClickListener(this);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemLongClickListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        clearActionMode();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isAutoRefresh = pref.getBoolean("auto_refresh?", true);

        refreshIf((isToday || isSingle) && isAutoRefresh && !isRefreshed);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        refreshIf(isVisibleToUser && isAutoRefresh && !isRefreshed);
    }

    @Override
    public void onDestroy() {
        Crouton.cancelAllCroutons();

        super.onDestroy();
    }

    @Override
    protected boolean shouldCleanListChoice() {
        int position = mListView.getCheckedItemPosition();
        return mListView.getFirstVisiblePosition() > position
                || mListView.getLastVisiblePosition() < position;
    }

    @Override
    protected void clearListChoice() {
        mListView.clearChoices();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void markItemCheckedAtPosition(int position) {
        mListView.setItemChecked(position, true);
    }

    private void refreshIf(boolean prerequisite) {
        if (prerequisite) {
            doRefresh();
        }
    }

    private void doRefresh() {
        if (isToday) {
            new OriginalGetNewsTask(date, this).execute();
        } else {
            SharedPreferences sharedPreferences
                    = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPreferences.getBoolean("using_accelerate_server?", false)) {
                String serverCode;
                if (sharedPreferences.getString("which_accelerate_server", Constants.ServerCode.SAE)
                        .equals(Constants.ServerCode.SAE)) {
                    serverCode = Constants.ServerCode.SAE;
                } else {
                    serverCode = Constants.ServerCode.HEROKU;
                }
                new AccelerateGetNewsTask(serverCode, date, this).execute();
            } else {
                new OriginalGetNewsTask(date, this).execute();
            }
        }
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    @Override
    public void beforeTaskStart() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void afterTaskFinished(List<DailyNews> resultList, boolean isRefreshSuccess, boolean isContentSame) {
        clearActionMode();
        mSwipeRefreshLayout.setRefreshing(false);
        isRefreshed = true;

        if (isRefreshSuccess) {
            if (!isContentSame) {
                newsList = resultList;
                listAdapter.updateNewsList(newsList);
            }
        } else if (isAdded()) {
            Crouton.makeText(getActivity(), getActivity().getString(R.string.network_error), Style.ALERT).show();
        }
    }

    private class RecoverNewsListTask extends MyAsyncTask<Void, Void, List<DailyNews>> {

        @Override
        protected List<DailyNews> doInBackground(Void... params) {
            return ZhihuDailyPurifyApplication.getDataSource().newsOfTheDay(date);
        }

        @Override
        protected void onPostExecute(List<DailyNews> newsListRecovered) {
            if (newsListRecovered != null) {
                newsList = newsListRecovered;
                listAdapter.updateNewsList(newsListRecovered);
            }
        }
    }
}