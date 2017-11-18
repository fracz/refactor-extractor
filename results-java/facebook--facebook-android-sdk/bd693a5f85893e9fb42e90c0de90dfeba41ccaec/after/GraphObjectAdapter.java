/**
 * Copyright 2012 Facebook
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.*;
import com.facebook.android.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.*;

class GraphObjectAdapter<T extends GraphObject> extends BaseAdapter implements SectionIndexer {
    private static final PrioritizedWorkQueue downloadWorkQueue = new PrioritizedWorkQueue();

    private final int DISPLAY_SECTIONS_THRESHOLD = 1;
    private final int HEADER_VIEW_TYPE = 0;
    private final int GRAPH_OBJECT_VIEW_TYPE = 1;
    private final String ID = "id";
    private final String NAME = "name";
    private final String PICTURE = "picture";

    private final LayoutInflater inflater;
    private List<String> sectionKeys = new ArrayList<String>();
    private Map<String, ArrayList<T>> graphObjectsBySection = new HashMap<String, ArrayList<T>>();
    private Map<String, T> allGraphObjects = new HashMap<String, T>();
    private boolean displaySections;
    private Set<String> selectedGraphObjectIds = new HashSet<String>();
    private List<String> sortFields;
    private String groupByField;
    private PictureDownloader pictureDownloader;
    private boolean showPicture;
    private boolean showCheckbox;

    private SelectionStyle selectionStyle;
    private final String TAG = "GraphObjectAdapter";

    public enum SelectionStyle {
        SINGLE_SELECT,
        MULTI_SELECT
    }

    public GraphObjectAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public List<String> getSortFields() {
        return sortFields;
    }

    public void setSortFields(List<String> sortFields) {
        this.sortFields = sortFields;
    }

    public String getGroupByField() {
        return groupByField;
    }

    public void setGroupByField(String groupByField) {
        this.groupByField = groupByField;
    }

    public boolean getShowPicture() {
        return showPicture;
    }

    public void setShowPicture(boolean showPicture) {
        this.showPicture = showPicture;
    }

    public SelectionStyle getSelectionStyle() {
        return selectionStyle;
    }

    public void setSelectionStyle(SelectionStyle selectionStyle) {
        this.selectionStyle = selectionStyle;
    }

    public boolean getShowCheckbox() {
        return showCheckbox;
    }

    public void setShowCheckbox(boolean showCheckbox) {
        this.showCheckbox = showCheckbox;
    }

    public void add(Collection<T> graphObjects) {
        if (graphObjects.size() == 0) {
            return;
        }

        for (T graphObject : graphObjects) {
            allGraphObjects.put(getIdOfGraphObject(graphObject), graphObject);
        }
        rebuildSections();

        notifyDataSetChanged();
    }

    public void clear() {
        allGraphObjects = new HashMap<String, T>();
        selectedGraphObjectIds = new HashSet<String>();
        rebuildSections();

        notifyDataSetChanged();
    }

    public void cancelPendingDownloads() {
        PictureDownloader downloader = pictureDownloader;
        if (downloader != null) {
            downloader.cancelAllDownloads();
        }
    }

    public void prioritizeViewRange(int start, int count) {
        PictureDownloader downloader = pictureDownloader;
        if (downloader != null) {
            downloader.prioritizeViewRange(start, count);
        }
    }

    protected String getSectionKeyOfGraphObject(T graphObject) {
        String result = null;

        if (groupByField != null) {
            result = (String) graphObject.get(groupByField);
            if (result != null && result.length() > 0) {
                result = result.substring(0, 1).toUpperCase();
            }
        }

        return (result != null) ? result : "";
    }

    protected boolean filterIncludesItem(T graphObject) {
        // TODO implement searching/filtering logic
        return true;
    }

    protected CharSequence getTitleOfGraphObject(T graphObject) {
        return (String) graphObject.get(NAME);
    }

    protected CharSequence getSubTitleOfGraphObject(T graphObject) {
        return null;
    }

    protected String getIdOfGraphObject(T graphObject) {
        if (graphObject.containsKey(ID)) {
            Object obj = graphObject.get(ID);
            if (obj instanceof String) {
                return (String) obj;
            }
        }
        throw new FacebookException("Received an object without an ID.");
    }

    protected URL getPictureUrlOfGraphObject(T graphObject) {
        String url = null;
        Object o = graphObject.get(PICTURE);
        if (o instanceof String) {
            url = (String) o;
        } else if (o instanceof GraphObject) {
            ItemPictureData data = ((ItemPicture)o).getData();
            if (data != null) {
                url = data.getUrl();
            }
        }

        if (url != null) {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
            }
        }
        return null;
    }

    protected View getSectionHeaderView(String sectionHeader, View convertView, ViewGroup parent) {
        TextView result = (TextView) convertView;

        if (result == null) {
            result = (TextView) inflater.inflate(R.layout.list_section_header, null);
        }

        result.setText(sectionHeader);

        return result;
    }

    protected View getGraphObjectView(T graphObject, View convertView, ViewGroup parent) {
        View result = (View) convertView;

        if (result == null) {
            result = createGraphObjectView(graphObject, convertView, parent);
        }

        populateGraphObjectView(result, graphObject);
        return result;
    }

    protected int getGraphObjectRowLayoutId(T graphObject) {
        return R.layout.profile_picker_list_row;
    }

    protected int getDefaultPicture() {
        return R.drawable.profile_default_icon;
    }

    protected View createGraphObjectView(T graphObject, View convertView, ViewGroup parent) {
        View result = inflater.inflate(getGraphObjectRowLayoutId(graphObject), null);

        ViewStub checkboxStub = (ViewStub) result.findViewById(R.id.checkbox_stub);
        if (checkboxStub != null) {
            if (!getShowCheckbox()) {
                checkboxStub.setVisibility(View.GONE);
            } else {
                CheckBox checkBox = (CheckBox) checkboxStub.inflate();
                updateCheckboxState(checkBox, false);
            }
        }

        ViewStub profilePicStub = (ViewStub) result.findViewById(R.id.profile_pic_stub);
        if (!getShowPicture()) {
            profilePicStub.setVisibility(View.GONE);
        } else {
            ImageView imageView = (ImageView) profilePicStub.inflate();
            imageView.setVisibility(View.VISIBLE);
        }

        return result;
    }

    protected void populateGraphObjectView(View view, T graphObject) {
        String id = getIdOfGraphObject(graphObject);
        view.setTag(id);

        CharSequence title = getTitleOfGraphObject(graphObject);
        TextView titleView = (TextView) view.findViewById(R.id.picker_title);
        if (titleView != null) {
            titleView.setText(title, TextView.BufferType.SPANNABLE);
        }

        CharSequence subtitle = getSubTitleOfGraphObject(graphObject);
        TextView subtitleView = (TextView) view.findViewById(R.id.picker_subtitle);
        if (subtitleView != null) {
            if (subtitle != null) {
                subtitleView.setText(subtitle, TextView.BufferType.SPANNABLE);
                subtitleView.setVisibility(View.VISIBLE);
            } else {
                subtitleView.setVisibility(View.GONE);
            }
        }

        if (getShowCheckbox()) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.picker_checkbox);
            updateCheckboxState(checkBox, selectedGraphObjectIds.contains(id));
        }

        if (getShowPicture()) {
            URL pictureURL = getPictureUrlOfGraphObject(graphObject);

            ImageView profilePic = (ImageView) view.findViewById(R.id.picker_image);
            getPictureDownloader().download(id, pictureURL, profilePic);
        }
    }

    private PictureDownloader getPictureDownloader() {
        if (pictureDownloader == null) {
            pictureDownloader = new PictureDownloader();
        }

        return pictureDownloader;
    }

    private void updateCheckboxState(CheckBox checkBox, boolean graphObjectSelected) {
        // In single-select mode, we only display the checkbox if checked. In multi-select
        // mode, we display a grayed-out checkbox for all non-selected rows.
        if (!graphObjectSelected && selectionStyle == SelectionStyle.SINGLE_SELECT) {
            checkBox.setVisibility(View.GONE);
        } else {
            checkBox.setChecked(graphObjectSelected);
            checkBox.setVisibility(View.VISIBLE);
        }
    }

    private void rebuildSections() {
        sectionKeys = new ArrayList<String>();
        graphObjectsBySection = new HashMap<String, ArrayList<T>>();

        int objectsAdded = 0;
        for (T graphObject : allGraphObjects.values()) {
            if (!filterIncludesItem(graphObject)) {
                continue;
            }

            objectsAdded++;

            String sectionKeyOfItem = getSectionKeyOfGraphObject(graphObject);
            if (!graphObjectsBySection.containsKey(sectionKeyOfItem)) {
                sectionKeys.add(sectionKeyOfItem);
                graphObjectsBySection.put(sectionKeyOfItem, new ArrayList<T>());
            }
            List<T> section = graphObjectsBySection.get(sectionKeyOfItem);
            section.add(graphObject);
        }

        if (sortFields != null) {
            final Collator collator = Collator.getInstance();
            for (List<T> section : graphObjectsBySection.values()) {
                Collections.sort(section, new Comparator<GraphObject>() {
                    // TODO pull this out into Utility method?
                    @Override
                    public int compare(GraphObject a, GraphObject b) {
                        return Utility.compareGraphObjects(a, b, sortFields, collator);
                    }
                });
            }
        }

        Collections.sort(sectionKeys, Collator.getInstance());

        displaySections = sectionKeys.size() > 1 && objectsAdded > DISPLAY_SECTIONS_THRESHOLD;
    }

    Pair<String, T> getSectionAndItem(int position) {
        if (sectionKeys.size() == 0) {
            return null;
        }
        String sectionKey = null;
        T graphObject = null;

        if (!displaySections) {
            sectionKey = sectionKeys.get(0);
            graphObject = graphObjectsBySection.get(sectionKey).get(position);
        } else {
            // Count through the sections; the "0" position in each section is the header. We decrement
            // position each time we skip forward a certain number of elements, including the header.
            for (String key : sectionKeys) {
                // Decrement if we skip over the header
                if (position-- == 0) {
                    sectionKey = key;
                    break;
                }

                List<T> section = graphObjectsBySection.get(key);
                if (position < section.size()) {
                    // The position is somewhere in this section. Get the corresponding graph object.
                    sectionKey = key;
                    graphObject = section.get(position);
                    break;
                }
                // Decrement by as many items as we skipped over
                position -= section.size();
            }
        }
        if (sectionKey != null) {
            // Note: graphObject will be null if this represents a section header.
            return new Pair<String, T>(sectionKey, graphObject);
        } else {
            return null;
        }
    }

    int getPosition(String sectionKey, T graphObject) {
        int position = 0;
        boolean found = false;

        // First find the section key and increment position one for each header we will render;
        // increment by the size of each section prior to the one we want.
        for (String key : sectionKeys) {
            if (displaySections) {
                position++;
            }
            if (key.equals(sectionKey)) {
                found = true;
                break;
            } else {
                position += graphObjectsBySection.get(key).size();
            }
        }

        if (!found) {
            return -1;
        } else if (graphObject == null) {
            // null represents the header for a section; we counted this header in position earlier,
            // so subtract it back out.
            return position - (displaySections ? 1 : 0);
        }

        // Now find index of this item within that section.
        for (T t : graphObjectsBySection.get(sectionKey)) {
            if (GraphObjectWrapper.hasSameId(t, graphObject)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    @Override
    public int getCount() {
        if (sectionKeys.size() == 0) {
            return 0;
        }

        // If we are not displaying sections, we don't display a header; otherwise, we have one header per item in
        // addition to the actual items.
        int count = (displaySections) ? sectionKeys.size() : 0;
        for (List<T> section : graphObjectsBySection.values()) {
            count += section.size();
        }
        return count;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return displaySections;
    }

    @Override
    public boolean isEnabled(int position) {
        Pair<String, T> pair = getSectionAndItem(position);
        return pair.second != null;
    }

    @Override
    public Object getItem(int position) {
        Pair<String, T> pair = getSectionAndItem(position);
        return (pair != null) ? pair.second : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Pair<String, T> pair = getSectionAndItem(position);
        return (pair.second != null) ? GRAPH_OBJECT_VIEW_TYPE : HEADER_VIEW_TYPE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Pair<String, T> pair = getSectionAndItem(position);

        if (pair.second == null) {
            return getSectionHeaderView(pair.first, convertView, parent);
        } else {
            return getGraphObjectView(pair.second, convertView, parent);
        }
    }

    @Override
    public Object[] getSections() {
        if (displaySections) {
            return sectionKeys.toArray();
        } else {
            return new Object[0];
        }
    }

    @Override
    public int getPositionForSection(int section) {
        if (displaySections) {
            section = Math.max(0, Math.min(section, sectionKeys.size() - 1));
            if (section < sectionKeys.size()) {
                return getPosition(sectionKeys.get(section), null);
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        Pair<String, T> pair = getSectionAndItem(position);
        if (pair != null) {
            return Math.max(0, Math.min(sectionKeys.indexOf(pair.first), sectionKeys.size() - 1));
        }
        return 0;
    }


    public void toggleSelection(T graphObject, ListView listView) {
        String id = getIdOfGraphObject(graphObject);

        Set<String> selectIds = new HashSet<String>();
        Set<String> deselectIds = new HashSet<String>();

        if (selectedGraphObjectIds.contains(id)) {
            // Un-select the graph object
            deselectIds.add(id);
        } else {
            // Select the graph object
            selectIds.add(id);
            if (selectionStyle == SelectionStyle.SINGLE_SELECT) {
                deselectIds.addAll(selectedGraphObjectIds);
            }
        }

        selectedGraphObjectIds.removeAll(deselectIds);
        selectedGraphObjectIds.addAll(selectIds);

        if (getShowCheckbox()) {
            int first = listView.getFirstVisiblePosition();
            int last = listView.getLastVisiblePosition();
            for (int i = first; i <= last; ++i) {
                Pair<String, T> sectionAndItem = getSectionAndItem(i);
                if (sectionAndItem.second != null) {
                    View view = listView.getChildAt(i - first);
                    boolean check = selectedGraphObjectIds.contains(getIdOfGraphObject(sectionAndItem.second));
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.picker_checkbox);
                    updateCheckboxState(checkBox, check);
                }
            }
        }
    }

    public Set<T> getSelectedGraphObjects() {
        Set<T> result = new HashSet<T>(selectedGraphObjectIds.size());
        for (String id : selectedGraphObjectIds) {
            T graphObject = allGraphObjects.get(id);
            if (graphObject != null) {
                result.add(graphObject);
            }
        }
        return result;
    }

    private class PictureDownloader {
        private final Map<String, PictureDownload> pendingDownloads = new HashMap<String, PictureDownload>();
        private final Handler handler = new Handler();

        void download(String id, URL pictureURL, ImageView imageView) {
            validateIsUIThread(true);

            if (!pictureURL.equals(imageView.getTag())) {
                imageView.setTag(id);

                PictureDownload download = new PictureDownload(id, pictureURL, imageView);
                InputStream stream = ImageResponseCache.getCachedImageStream(pictureURL, imageView.getContext());

                if (stream != null) {
                    updateView(download, stream);
                } else {
                    imageView.setImageResource(getDefaultPicture());
                    start(download);
                }
            }
        }

        void cancelAllDownloads() {
            validateIsUIThread(true);

            for (PictureDownload download : pendingDownloads.values()) {
                download.workItem.cancel();
            }

            pendingDownloads.clear();
        }

        void prioritizeViewRange(int start, int count) {
            validateIsUIThread(true);

            downloadWorkQueue.backgroundAll();
            for (int i = start; i < (start + count); i++) {
                Pair<String,T> pair = getSectionAndItem(i);
                if ((pair != null) && (pair.second != null)) {
                    String id = getIdOfGraphObject(pair.second);
                    PictureDownload download = pendingDownloads.get(id);
                    if (download != null) {
                        download.workItem.setPriority(PrioritizedWorkQueue.PRIORITY_ACTIVE);
                    }
                }
            }
        }

        private void start(final PictureDownload download) {
            validateIsUIThread(true);

            if (pendingDownloads.containsKey(download.graphObjectId)) {
                PictureDownload inProgress = pendingDownloads.get(download.graphObjectId);
                inProgress.imageView = download.imageView;
            } else {
                pendingDownloads.put(download.graphObjectId, download);
                download.workItem = downloadWorkQueue.addActiveWorkItem(new Runnable() {
                    @Override
                    public void run() {
                        getStream(download);
                    }
                });
            }
        }

        private void getStream(final PictureDownload download) {
            validateIsUIThread(false);

            try {
                final InputStream stream = ImageResponseCache.getImageStream(download.pictureURL, download.context);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateView(download, stream);
                    }
                });

            } catch (IOException e) {
            }
        }

        private void updateView(final PictureDownload download, final InputStream stream) {
            validateIsUIThread(true);

            if (download.graphObjectId.equals(download.imageView.getTag())) {
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                download.imageView.setImageBitmap(bitmap);
                download.imageView.setTag(download.pictureURL);
            }
        }

        void validateIsUIThread(boolean uiThreadExpected) {
            assert uiThreadExpected == (handler.getLooper() == Looper.myLooper());
        }
    }

    private class PictureDownload {
        public final String graphObjectId;
        public final URL pictureURL;
        public final Context context;
        public ImageView imageView;
        public PrioritizedWorkQueue.WorkItem workItem;

        public PictureDownload(String graphObjectId, URL pictureURL, ImageView imageView) {
            this.graphObjectId = graphObjectId;
            this.pictureURL = pictureURL;
            this.imageView = imageView;
            context = imageView.getContext().getApplicationContext();
        }
    }

    // Graph object type to navigate the JSON that sometimes comes back instead of a URL string
    private interface ItemPicture extends GraphObject {
        ItemPictureData getData();
    }

    // Graph object type to navigate the JSON that sometimes comes back instead of a URL string
    private interface ItemPictureData extends GraphObject {
        String getUrl();
    }
}