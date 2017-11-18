package com.daimajia.swipedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.daimajia.swipe.util.Attributes;
import com.daimajia.swipedemo.adapter.RecyclerViewSimpleAdapter;

import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.TwoWayLayoutManager;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.ListLayoutManager;

public class RecyclerViewExample extends Activity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    // RecyclerView.LayoutManager: https://developer.android.com/reference/android/support/v7/widget/RecyclerView.LayoutManager.html
    // Our LayoutManager uses: https://github.com/lucasr/twoway-view to help with decoration and can be used for a more advanced config as well.
    // Read http://lucasr.org/2014/07/31/the-new-twowayview/ for a better understanding
    private RecyclerView.LayoutManager mLayoutManager;
    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        /**
         * The following comment is the sample usage of ArraySwipeAdapter.
         */
        String[] adapterData = new String[]{"Activity", "Service", "Content Provider", "Intent", "BroadcastReceiver", "ADT", "Sqlite3", "HttpClient",
                "DDMS", "Android Studio", "Fragment", "Loader", "Activity", "Service", "Content Provider", "Intent",
                "BroadcastReceiver", "ADT", "Sqlite3", "HttpClient", "Activity", "Service", "Content Provider", "Intent",
                "BroadcastReceiver", "ADT", "Sqlite3", "HttpClient"};

        // Uses a ListLayout manager from TwoWayView Lib:
        mLayoutManager = new ListLayoutManager(this, TwoWayLayoutManager.Orientation.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        final Drawable divider = getResources().getDrawable(R.drawable.divider);
        recyclerView.addItemDecoration(new DividerItemDecoration(divider));

        mAdapter = new RecyclerViewSimpleAdapter(this, adapterData);
        recyclerView.setAdapter(mAdapter);
        ((RecyclerViewSimpleAdapter) mAdapter).setMode(Attributes.Mode.Single);

        /* Listeners */
        ItemClickSupport itemClick = ItemClickSupport.addTo(recyclerView);
        itemClick.setOnItemClickListener(onItemClickListener);
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("ListView", "OnTouch");
                return false;
            }
        });
        recyclerView.setOnScrollListener(onScrollListener);

        // TODO: Item Long Click is firing for every touch.
//        itemClick.setOnItemLongClickListener(onItemLongClickListener);

        // TODO: Item Selection Support for RecyclerView
//        recyclerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.e("ListView", "onItemSelected:" + position);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                Log.e("ListView", "onNothingSelected:");
//            }
//        });

    }

    /**
     * Substitute for our onItemClick listener for RecyclerView
     */
    ItemClickSupport.OnItemClickListener onItemClickListener = new ItemClickSupport.OnItemClickListener() {
        @Override
        public void onItemClick(RecyclerView parent, View child, int position, long id) {
            Toast.makeText(mContext, "Clicked:" + position, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Substitute for our onItemLongClick listener for RecyclerView
     */
    ItemClickSupport.OnItemLongClickListener onItemLongClickListener = new ItemClickSupport.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(RecyclerView recyclerView, View view, int position, long id) {
            Toast.makeText(mContext, "OnItemLongClickListener:" + position, Toast.LENGTH_SHORT).show();
            return false;
        }
    };

    /**
     * Substitute for our onScrollListener for RecyclerView
     */
    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Log.e("ListView", "onScrollStateChanged");
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_listview) {
            startActivity(new Intent(this, ListViewExample.class));
            finish();
            return true;
        } else if (id == R.id.action_gridview) {
            startActivity(new Intent(this, GridViewExample.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}