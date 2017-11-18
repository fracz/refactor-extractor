/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.customize;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.TileService;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.statusbar.phone.QSTileHost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TileQueryHelper {

    private static final String TAG = "TileQueryHelper";

    private final ArrayList<TileInfo> mTiles = new ArrayList<>();
    private final ArrayList<String> mSpecs = new ArrayList<>();
    private final Context mContext;
    private TileStateListener mListener;

    public TileQueryHelper(Context context, QSTileHost host) {
        mContext = context;
        addSystemTiles(host);
        // TODO: Live?
    }

    private void addSystemTiles(QSTileHost host) {
        boolean hasColorMod = host.getDisplayController().isEnabled();
        String possible = mContext.getString(R.string.quick_settings_tiles_default)
                + ",hotspot,inversion,saver" + (hasColorMod ? ",colors" : "");
        String[] possibleTiles = possible.split(",");
        final Handler qsHandler = new Handler(host.getLooper());
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < possibleTiles.length; i++) {
            final String spec = possibleTiles[i];
            final QSTile<?> tile = host.createTile(spec);
            if (tile == null) {
                continue;
            }
            tile.setListening(true);
            tile.clearState();
            tile.refreshState();
            tile.setListening(false);
            qsHandler.post(new Runnable() {
                @Override
                public void run() {
                    final QSTile.State state = tile.newTileState();
                    tile.getState().copyTo(state);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            addTile(spec, state);
                            mListener.onTilesChanged(mTiles);
                        }
                    });
                }
            });
        }
        qsHandler.post(new Runnable() {
            @Override
            public void run() {
                new QueryTilesTask().execute();
            }
        });
    }

    public void setListener(TileStateListener listener) {
        mListener = listener;
    }

    private void addTile(String spec, QSTile.State state) {
        if (mSpecs.contains(spec)) {
            return;
        }
        TileInfo info = new TileInfo();
        info.state = state;
        info.spec = spec;
        mTiles.add(info);
        mSpecs.add(spec);
    }

    private void addTile(String spec, Drawable drawable, CharSequence label, Context context) {
        QSTile.State state = new QSTile.State();
        state.label = label;
        state.contentDescription = label;
        state.icon = new DrawableIcon(drawable);
        addTile(spec, state);
    }

    public static class TileInfo {
        public String spec;
        public QSTile.State state;
    }

    private class QueryTilesTask extends AsyncTask<Void, Void, Collection<TileInfo>> {
        @Override
        protected Collection<TileInfo> doInBackground(Void... params) {
            List<TileInfo> tiles = new ArrayList<>();
            PackageManager pm = mContext.getPackageManager();
            List<ResolveInfo> services = pm.queryIntentServicesAsUser(
                    new Intent(TileService.ACTION_QS_TILE), 0, ActivityManager.getCurrentUser());
            for (ResolveInfo info : services) {
                String packageName = info.serviceInfo.packageName;
                ComponentName componentName = new ComponentName(packageName, info.serviceInfo.name);
                String spec = CustomTile.toSpec(componentName);
                Drawable icon = info.serviceInfo.loadIcon(pm);
                if (icon != null) {
                    icon.mutate();
                    icon.setTint(mContext.getColor(android.R.color.white));
                }
                CharSequence label = info.serviceInfo.loadLabel(pm);
                addTile(spec, icon, label != null ? label.toString() : "null", mContext);
            }
            return tiles;
        }

        @Override
        protected void onPostExecute(Collection<TileInfo> result) {
            mTiles.addAll(result);
            mListener.onTilesChanged(mTiles);
        }
    }

    public interface TileStateListener {
        void onTilesChanged(List<TileInfo> tiles);
    }
}