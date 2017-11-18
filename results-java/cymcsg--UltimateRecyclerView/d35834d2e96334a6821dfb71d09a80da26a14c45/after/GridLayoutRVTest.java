package com.marshalchen.ultimaterecyclerview.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.demo.modules.SampleDataboxset;
import com.marshalchen.ultimaterecyclerview.grid.BasicGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hesk on 24/8/15.
 */
public class GridLayoutRVTest extends AppCompatActivity {
    private UltimateRecyclerView listuv;
    private GridLayoutRVAdapter mGridAdapter = null;
    private BasicGridLayoutManager mGridLayoutManager;
    private int moreNum = 2, columns = 2;
    private ActionMode actionMode;
    private Toolbar mToolbar;
    boolean isDrag = true;
    private ItemTouchHelper mItemTouchHelper;
    public static final String TAG = "GLV";

    @LayoutRes
    protected int getMainLayout() {
        return R.layout.floatingbutton_grid_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getMainLayout());
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        listuv = (UltimateRecyclerView) findViewById(R.id.ultimate_recycler_view);
        mGridAdapter = new GridLayoutRVAdapter(getItems());
        mGridAdapter.setSpanColumns(columns);
        mGridLayoutManager = new BasicGridLayoutManager(this, columns, mGridAdapter);
        listuv.setLayoutManager(mGridLayoutManager);
        listuv.setHasFixedSize(true);
        listuv.setSaveEnabled(true);
        listuv.setClipToPadding(false);
        listuv.setAdapter(mGridAdapter);
        listuv.setItemAnimator(new DefaultItemAnimator());

        // mGridAdapter.setCustomLoadMoreView(LayoutInflater.from(this).inflate(R.layout.custom_bottom_progressbar, null));
        listuv.setNormalHeader(setupHeaderView());
        final Handler f = new Handler();
        listuv.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                //   Log.d(TAG, itemsCount + " :: " + itemsCount);
                f.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mGridAdapter.insert(SampleDataboxset.newListFromGen(2));
                        // listuv.disableLoadmore();
                        // listuv.disableLoadmore();
                    }
                }, 1000);
            }
        });

        listuv.enableLoadmore();
        //    listuv.disableLoadmore();
        harn_controls();
    }

    private List<String> getItems() {
        List<String> team = new ArrayList<>();
        team.add("1 YU 0");
        team.add("2 BF H");
        team.add("3 AF HH");
        team.add("4 FR HHH");
        team.add("5 LAST SECOND");
        team.add("6 FE LAST");
        return team;
    }

    private void dimension_columns() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        float dpWidth = outMetrics.widthPixels / density;
        columns = Math.round(dpWidth / 300);
    }


    private View setupHeaderView() {
        View custom_header = LayoutInflater.from(this).inflate(R.layout.header_love, null, false);


        return custom_header;
    }

    private void harn_controls() {
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> f = new ArrayList<String>();
                f.add("ONE@1");
                f.add("ONE@2");
                f.add("ONE@3");

                mGridAdapter.insert(f);
            }
        });

        findViewById(R.id.del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridAdapter.removeLast();
            }
        });

        findViewById(R.id.delall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.add_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridAdapter.insert("single ONE@PLUS");
            }
        });

        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  mGridAdapter.notifyDataSetChanged();
            }
        });


    }

}