package com.octo.android.robospice.spicelist;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import roboguice.util.temp.Ln;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.BigBinaryRequest;

/**
 * An adapter that is optimized for {@link SpiceListView} instances. It offers
 * to update ImageViews contained in {@link SpiceListItemView} instances with
 * images loaded from the network. All you have to do is to Override
 * {@link #createRequest(Object)} to define a request for each object in the
 * list that is associated an image to display. Also please note that in your
 * {@link #getView(int, android.view.View, android.view.ViewGroup)} method, you
 * must call
 * {@link #updateListItemViewAsynchronously(Object, SpiceListItemView)} in order
 * for your {@link SpiceListItemView} to be updated automagically.
 * @author sni
 * @param <T>
 *            the type of data displayed by the list.
 */
public abstract class SpiceArrayAdapter<T> extends ArrayAdapter<T> {

    private static final int IMG_WIDTH = 45;
    private static final float DP_TO_PX_CONVERSION_FACTOR = 160f;
    /**
     * Indicates wether to use the network to update data. This is set by the
     * {@link SpiceListView}.
     */
    private boolean isNetworkFetchingAllowed = true;
    /**
     * A {@link SpiceManager} that will be used to fetch binaries. It's
     * lifecycle has to be managed at the context level (usually fragment or
     * activity).
     */
    private SpiceManager spiceManagerBinary;
    /**
     * List of event listeners to get notified of network fetching allowed
     * changes.
     */
    private List<NetworkFetchingAuthorizationStateChangeAdapter> networkFetchingAuthorizationStateChangeListenerList = Collections
        .synchronizedList(new ArrayList<NetworkFetchingAuthorizationStateChangeAdapter>());
    /**
     * Contains all images that have been added recently to the list. They will
     * be animated when first displayed.
     */
    private Set<Object> freshDrawableSet = new HashSet<Object>();
    /** The default drawable to display during image loading from the network. */
    private Drawable defaultDrawable;

    // ----------------------------
    // --- CONSTRUCTOR
    // ----------------------------

    public SpiceArrayAdapter(Context context,
        BigBinarySpiceManager spiceManagerBinary, List<T> objects) {
        super(context, 0, objects);
        this.spiceManagerBinary = spiceManagerBinary;
        defaultDrawable = context.getResources().getDrawable(
            android.R.drawable.picture_frame);
    }

    public SpiceArrayAdapter(Context context,
        BigBinarySpiceManager spiceManagerBinary) {
        this(context, spiceManagerBinary, new ArrayList<T>());
    }

    public SpiceArrayAdapter(Context context,
        BigBinarySpiceManager spiceManagerBinary, T[] objects) {
        this(context, spiceManagerBinary, Arrays.asList(objects));
    }

    /**
     * Used for testing only.
     */
    protected SpiceArrayAdapter(Context context,
        SpiceManager spiceManagerBinary, List<T> objects) {
        super(context, 0, objects);
        this.spiceManagerBinary = spiceManagerBinary;
        defaultDrawable = context.getResources().getDrawable(
            android.R.drawable.picture_frame);
    }

    // ----------------------------
    // --- PUBLIC API
    // ----------------------------

    public void setDefaultUserDrawable(Drawable defaultUserDrawable) {
        this.defaultDrawable = defaultUserDrawable;
    }

    /* package-private */void setNetworkFetchingAllowed(
        boolean isNetworkFetchingAllowed) {
        boolean changed = isNetworkFetchingAllowed != this.isNetworkFetchingAllowed;
        this.isNetworkFetchingAllowed = isNetworkFetchingAllowed;
        if (isNetworkFetchingAllowed && changed) {
            fireOnNetworkFetchingAllowedChange();
            Ln.d("calling state change listeners");
        }
    }

    /**
     * Updates a {@link SpiceListItemView} containing some data. The method
     * {@link #createRequest(Object)} will be applied to data to know which
     * request to execute to get data from network if needed. This method must
     * be called during
     * {@link #getView(int, android.view.View, android.view.ViewGroup)}.
     * @param data
     *            the data to update the {@link SpiceListItemView} with.
     * @param view
     *            the {@link SpiceListItemView} that displays an image to
     *            represent data.
     */
    protected void updateListItemViewAsynchronously(T data,
        SpiceListItemView<T> view) {
        if (!registered(view)) {
            addSpiceListItemView(view);
        }
        new ThumbnailAsynTask(createRequest(data)).execute(data, view);
    }

    /**
     * Returns the {@link SpiceRequest} used to update the image associated to a
     * given data.
     * @param data
     *            the data whose image will be fetched from network by this
     *            query.
     * @return the {@link SpiceRequest} used to update the image associated to a
     *         given data.
     */
    public abstract BigBinaryRequest createRequest(T data);

    // ----------------------------
    // --- PRIVATE API
    // ----------------------------

    private void addSpiceListItemView(SpiceListItemView<T> spiceListItemView) {
        this.networkFetchingAuthorizationStateChangeListenerList
            .add(new NetworkFetchingAuthorizationStateChangeAdapter(
                spiceListItemView));
    }

    private boolean registered(SpiceListItemView<T> view) {
        for (NetworkFetchingAuthorizationStateChangeAdapter listener : networkFetchingAuthorizationStateChangeListenerList) {
            if (listener.getView() == view) {
                return true;
            }
        }
        return false;
    }

    // protected for testing purposes
    protected void loadBitmapAsynchronously(T octo, ImageView thumbImageView,
        String tempThumbnailImageFileName) {
        if (thumbImageView.getTag() != null
            && thumbImageView.getTag().equals(tempThumbnailImageFileName)) {
            return;
        }

        if (cancelPotentialWork(tempThumbnailImageFileName, thumbImageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(thumbImageView,
                octo);
            task.fileName = tempThumbnailImageFileName;
            final AsyncDrawable asyncDrawable = new AsyncDrawable(getContext()
                .getResources(), task);
            thumbImageView.setImageDrawable(asyncDrawable);
            thumbImageView.setTag(tempThumbnailImageFileName);
            int width = Math.round(convertDpToPixel(IMG_WIDTH, getContext()));
            int height = width;
            task.execute(tempThumbnailImageFileName, String.valueOf(width),
                String.valueOf(height));
        }
    }

    private void fireOnNetworkFetchingAllowedChange() {
        synchronized (networkFetchingAuthorizationStateChangeListenerList) {
            for (NetworkFetchingAuthorizationStateChangeAdapter networkFetchingAuthorizationStateChangeListener : networkFetchingAuthorizationStateChangeListenerList) {
                Ln.d("calling state change listener");
                networkFetchingAuthorizationStateChangeListener
                    .onNetworkFetchingAllowedChange(isNetworkFetchingAllowed);
            }
        }
    }

    private boolean cancelPotentialWork(String fileName, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapFileName = bitmapWorkerTask.fileName;
            if (bitmapFileName == null || !bitmapFileName.equals(fileName)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was
        // cancelled
        return true;
    }

    private BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof SpiceArrayAdapter.AsyncDrawable) {
                @SuppressWarnings("unchecked")
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private static Bitmap decodeSampledBitmapFromResource(String fileName,
        int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
            reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileName, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
        int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    // ----------------------------
    // --- INNER CLASSES
    // ----------------------------

    private class OctoImageRequestListener implements
        RequestListener<InputStream> {

        private SpiceListItemView<T> spiceListItemView;
        private T octo;
        private ImageView thumbImageView;
        private String imageFileName;

        public OctoImageRequestListener(T octo,
            SpiceListItemView<T> spiceListItemView, String imageFileName) {
            this.octo = octo;
            this.spiceListItemView = spiceListItemView;
            this.thumbImageView = spiceListItemView.getImageView();
            this.imageFileName = imageFileName;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Ln.e(SpiceListItemView.class.getName(), "Unable to retrive image",
                spiceException);
            thumbImageView.setImageDrawable(defaultDrawable);
        }

        @Override
        public void onRequestSuccess(InputStream result) {
            freshDrawableSet.add(this.octo);
            if (this.octo.equals(spiceListItemView.getData())) {
                loadBitmapAsynchronously(octo, thumbImageView, imageFileName);
            }
        }
    }

    protected class ThumbnailAsynTask extends AsyncTask<Object, Void, Boolean> {

        private T octo;
        private SpiceListItemView<T> spiceListItemView;
        private String tempThumbnailImageFileName = "";
        private BigBinaryRequest request;

        public ThumbnailAsynTask(BigBinaryRequest request) {
            this.request = request;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Boolean doInBackground(Object... params) {
            octo = (T) params[0];
            spiceListItemView = (SpiceListItemView<T>) params[1];

            File tempThumbnailImageFile = request.getCacheFile();
            tempThumbnailImageFileName = tempThumbnailImageFile
                .getAbsolutePath();
            Ln.d("Filename : " + tempThumbnailImageFileName);

            if (!tempThumbnailImageFile.exists()) {
                if (isNetworkFetchingAllowed) {
                    OctoImageRequestListener octoImageRequestListener = new OctoImageRequestListener(
                        octo, spiceListItemView, tempThumbnailImageFileName);
                    spiceManagerBinary.execute(request,
                        "THUMB_IMAGE_" + octo.hashCode(),
                        DurationInMillis.NEVER, octoImageRequestListener);
                }
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isImageAvailableInCache) {
            if (isImageAvailableInCache) {
                loadBitmapAsynchronously(octo,
                    spiceListItemView.getImageView(),
                    tempThumbnailImageFileName);
            } else {
                spiceListItemView.getImageView().setImageDrawable(
                    defaultDrawable);
            }
        }
    }

    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        String fileName = "";
        private T data;
        private Animation animation;

        public BitmapWorkerTask(ImageView imageView, T data) {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.data = data;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            animation = AnimationUtils.loadAnimation(getContext(),
                android.R.anim.fade_in);
            animation.setDuration(getContext().getResources().getInteger(
                android.R.integer.config_mediumAnimTime));
            fileName = params[0];
            int width = Integer.valueOf(params[1]);
            int height = Integer.valueOf(params[2]);
            return decodeSampledBitmapFromResource(fileName, width, height);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                if (data.toString().equals("JFA")) {
                    Ln.d(data.toString() + " : cancel decoding bitmap");
                }
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                if (data.toString().equals("JFA")) {
                    Ln.d(data.toString() + " : bitmapworkertask");
                }
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    if (freshDrawableSet.contains(data)) {
                        freshDrawableSet.remove(data);
                        imageView.startAnimation(animation);
                    }
                    imageView.setImageBitmap(bitmap);
                    // no used anymore.
                    // imageView.setTag( null );
                }
            }
        }
    }

    private class NetworkFetchingAuthorizationStateChangeAdapter {

        private WeakReference<SpiceListItemView<T>> weakReferenceSpiceListItemView;

        public NetworkFetchingAuthorizationStateChangeAdapter(
            SpiceListItemView<T> spiceListItemView) {
            this.weakReferenceSpiceListItemView = new WeakReference<SpiceListItemView<T>>(
                spiceListItemView);
        }

        public void onNetworkFetchingAllowedChange(
            boolean networkFetchingAllowed) {
            SpiceListItemView<T> spiceListItemView = weakReferenceSpiceListItemView
                .get();
            if (spiceListItemView != null) {
                T data = spiceListItemView.getData();
                new ThumbnailAsynTask(createRequest(data)).execute(data,
                    spiceListItemView);
            }
        }

        public SpiceListItemView<T> getView() {
            return weakReferenceSpiceListItemView.get();
        }
    }

    private class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, BitmapWorkerTask bitmapWorkerTask) {
            super(res);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(
                bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * This method convets dp unit to equivalent device specific value in
     * pixels.
     * @see http
     *      ://stackoverflow.com/questions/4605527/converting-pixels-to-dp-in-
     *      android
     * @param dp
     *            A value in dp(Device independent pixels) unit. Which we need
     *            to convert into pixels
     * @param context
     *            Context to get resources and device specific display metrics
     * @return A float value to represent Pixels equivalent to dp according to
     *         device
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / DP_TO_PX_CONVERSION_FACTOR);
        return px;
    }

}