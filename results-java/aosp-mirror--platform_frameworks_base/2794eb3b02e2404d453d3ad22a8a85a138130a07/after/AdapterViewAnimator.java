/*
 * Copyright (C) 2010 The Android Open Source Project
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

package android.widget;

import java.util.ArrayList;
import java.util.HashMap;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Base class for a {@link AdapterView} that will perform animations
 * when switching between its views.
 *
 * @attr ref android.R.styleable#AdapterViewAnimator_inAnimation
 * @attr ref android.R.styleable#AdapterViewAnimator_outAnimation
 * @attr ref android.R.styleable#AdapterViewAnimator_animateFirstView
 * @attr ref android.R.styleable#AdapterViewAnimator_loopViews
 */
public abstract class AdapterViewAnimator extends AdapterView<Adapter>
        implements RemoteViewsAdapter.RemoteAdapterConnectionCallback {
    private static final String TAG = "RemoteViewAnimator";

    /**
     * The index of the current child, which appears anywhere from the beginning
     * to the end of the current set of children, as specified by {@link #mActiveOffset}
     */
    int mWhichChild = 0;

    /**
     * Whether or not the first view(s) should be animated in
     */
    boolean mAnimateFirstTime = true;

    /**
     *  Represents where the in the current window of
     *  views the current <code>mDisplayedChild</code> sits
     */
    int mActiveOffset = 0;

    /**
     * The number of views that the {@link AdapterViewAnimator} keeps as children at any
     * given time (not counting views that are pending removal, see {@link #mPreviousViews}).
     */
    int mNumActiveViews = 1;

    /**
     * Map of the children of the {@link AdapterViewAnimator}.
     */
    private HashMap<Integer, ViewAndIndex> mViewsMap = new HashMap<Integer, ViewAndIndex>();

    /**
     * List of views pending removal from the {@link AdapterViewAnimator}
     */
    ArrayList<Integer> mPreviousViews;

    /**
     * The index, relative to the adapter, of the beginning of the window of views
     */
    int mCurrentWindowStart = 0;

    /**
     * The index, relative to the adapter, of the end of the window of views
     */
    int mCurrentWindowEnd = -1;

    /**
     * The same as {@link #mCurrentWindowStart}, except when the we have bounded
     * {@link #mCurrentWindowStart} to be non-negative
     */
    int mCurrentWindowStartUnbounded = 0;

    /**
     * Handler to post events to the main thread
     */
    Handler mMainQueue;

    /**
     * Listens for data changes from the adapter
     */
    AdapterDataSetObserver mDataSetObserver;

    /**
     * The {@link Adapter} for this {@link AdapterViewAnimator}
     */
    Adapter mAdapter;

    /**
     * The {@link RemoteViewsAdapter} for this {@link AdapterViewAnimator}
     */
    RemoteViewsAdapter mRemoteViewsAdapter;

    /**
     * Specifies whether this is the first time the animator is showing views
     */
    boolean mFirstTime = true;

    /**
     * Specifies if the animator should wrap from 0 to the end and vice versa
     * or have hard boundaries at the beginning and end
     */
    boolean mLoopViews = true;

    /**
     * The width and height of some child, used as a size reference in-case our
     * dimensions are unspecified by the parent.
     */
    int mReferenceChildWidth = -1;
    int mReferenceChildHeight = -1;

    /**
     * In and out animations.
     */
    ObjectAnimator mInAnimation;
    ObjectAnimator mOutAnimation;

    private  ArrayList<View> mViewsToBringToFront;

    private static final int DEFAULT_ANIMATION_DURATION = 200;

    public AdapterViewAnimator(Context context) {
        super(context);
        initViewAnimator();
    }

    public AdapterViewAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.AdapterViewAnimator);
        int resource = a.getResourceId(
                com.android.internal.R.styleable.AdapterViewAnimator_inAnimation, 0);
        if (resource > 0) {
            setInAnimation(context, resource);
        } else {
            setInAnimation(getDefaultInAnimation());
        }

        resource = a.getResourceId(com.android.internal.R.styleable.AdapterViewAnimator_outAnimation, 0);
        if (resource > 0) {
            setOutAnimation(context, resource);
        } else {
            setOutAnimation(getDefaultOutAnimation());
        }

        boolean flag = a.getBoolean(
                com.android.internal.R.styleable.AdapterViewAnimator_animateFirstView, true);
        setAnimateFirstView(flag);

        mLoopViews = a.getBoolean(
                com.android.internal.R.styleable.AdapterViewAnimator_loopViews, false);

        a.recycle();

        initViewAnimator();
    }

    /**
     * Initialize this {@link AdapterViewAnimator}
     */
    private void initViewAnimator() {
        mMainQueue = new Handler(Looper.myLooper());
        mPreviousViews = new ArrayList<Integer>();
        mViewsToBringToFront = new ArrayList<View>();
    }

    private class ViewAndIndex {
        ViewAndIndex(View v, int i) {
            view = v;
            index = i;
        }
        View view;
        int index;
    }

    /**
     * This method is used by subclasses to configure the animator to display the
     * desired number of views, and specify the offset
     *
     * @param numVisibleViews The number of views the animator keeps in the {@link ViewGroup}
     * @param activeOffset This parameter specifies where the current index ({@link #mWhichChild})
     *        sits within the window. For example if activeOffset is 1, and numVisibleViews is 3,
     *        and {@link #setDisplayedChild(int)} is called with 10, then the effective window will
     *        be the indexes 9, 10, and 11. In the same example, if activeOffset were 0, then the
     *        window would instead contain indexes 10, 11 and 12.
     * @param shouldLoop If the animator is show view 0, and setPrevious() is called, do we
     *        we loop back to the end, or do we do nothing
     */
     void configureViewAnimator(int numVisibleViews, int activeOffset) {
        if (activeOffset > numVisibleViews - 1) {
            // Throw an exception here.
        }
        mNumActiveViews = numVisibleViews;
        mActiveOffset = activeOffset;
        mPreviousViews.clear();
        mViewsMap.clear();
        removeAllViewsInLayout();
        mCurrentWindowStart = 0;
        mCurrentWindowEnd = -1;
    }

    /**
     * This class should be overridden by subclasses to customize view transitions within
     * the set of visible views
     *
     * @param fromIndex The relative index within the window that the view was in, -1 if it wasn't
     *        in the window
     * @param toIndex The relative index within the window that the view is going to, -1 if it is
     *        being removed
     * @param view The view that is being animated
     */
    void animateViewForTransition(int fromIndex, int toIndex, View view) {
        if (fromIndex == -1) {
            mInAnimation.setTarget(view);
            mInAnimation.start();
        } else if (toIndex == -1) {
            mOutAnimation.setTarget(view);
            mOutAnimation.start();
        }
    }

    ObjectAnimator getDefaultInAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(null, "alpha", 0.0f, 1.0f);
        anim.setDuration(DEFAULT_ANIMATION_DURATION);
        return anim;
    }

    ObjectAnimator getDefaultOutAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(null, "alpha", 1.0f, 0.0f);
        anim.setDuration(DEFAULT_ANIMATION_DURATION);
        return anim;
    }

    /**
     * Sets which child view will be displayed.
     *
     * @param whichChild the index of the child view to display
     */
    public void setDisplayedChild(int whichChild) {
        if (mAdapter != null) {
            mWhichChild = whichChild;
            if (whichChild >= mAdapter.getCount()) {
                mWhichChild = mLoopViews ? 0 : mAdapter.getCount() - 1;
            } else if (whichChild < 0) {
                mWhichChild = mLoopViews ? mAdapter.getCount() - 1 : 0;
            }

            boolean hasFocus = getFocusedChild() != null;
            // This will clear old focus if we had it
            showOnly(mWhichChild);
            if (hasFocus) {
                // Try to retake focus if we had it
                requestFocus(FOCUS_FORWARD);
            }
        }
    }

    /**
     * To be overridden by subclasses. This method applies a view / index specific
     * transform to the child view.
     *
     * @param child
     * @param relativeIndex
     */
    void applyTransformForChildAtIndex(View child, int relativeIndex) {
    }

    /**
     * Returns the index of the currently displayed child view.
     */
    public int getDisplayedChild() {
        return mWhichChild;
    }

    /**
     * Manually shows the next child.
     */
    public void showNext() {
        setDisplayedChild(mWhichChild + 1);
    }

    /**
     * Manually shows the previous child.
     */
    public void showPrevious() {
        setDisplayedChild(mWhichChild - 1);
    }

    /**
     * Shows only the specified child. The other displays Views exit the screen,
     * optionally with the with the {@link #getOutAnimation() out animation} and
     * the specified child enters the screen, optionally with the
     * {@link #getInAnimation() in animation}.
     *
     * @param childIndex The index of the child to be shown.
     * @param animate Whether or not to use the in and out animations, defaults
     *            to true.
     */
    void showOnly(int childIndex, boolean animate) {
        showOnly(childIndex, animate, false);
    }

    private int modulo(int pos, int size) {
        if (size > 0) {
            return (size + (pos % size)) % size;
        } else {
            return 0;
        }
    }

    /**
     * Get the view at this index relative to the current window's start
     *
     * @param relativeIndex Position relative to the current window's start
     * @return View at this index, null if the index is outside the bounds
     */
    View getViewAtRelativeIndex(int relativeIndex) {
        if (relativeIndex >= 0 && relativeIndex <= mNumActiveViews - 1 && mAdapter != null) {
            int adapterCount =  mAdapter.getCount();
            int i = modulo(mCurrentWindowStartUnbounded + relativeIndex, adapterCount);
            return mViewsMap.get(i).view;
        }
        return null;
    }

    LayoutParams createOrReuseLayoutParams(View v) {
        final ViewGroup.LayoutParams currentLp = v.getLayoutParams();
        if (currentLp instanceof ViewGroup.LayoutParams) {
            LayoutParams lp = (LayoutParams) currentLp;
            return lp;
        }
        return new ViewGroup.LayoutParams(0, 0);
    }

    void refreshChildren() {
        for (int i = mCurrentWindowStart; i <= mCurrentWindowEnd; i++) {
            int index = modulo(i, mNumActiveViews);

            // get the fresh child from the adapter
            View updatedChild = mAdapter.getView(i, null, this);

            if (mViewsMap.containsKey(index)) {
                FrameLayout fl = (FrameLayout) mViewsMap.get(index).view;
                // flush out the old child
                fl.removeAllViewsInLayout();
                // add the new child to the frame, if it exists
                if (updatedChild != null) {
                    fl.addView(updatedChild);
                }
            }
        }
    }

    /**
     * This method can be overridden so that subclasses can provide a custom frame in which their
     * children can live. For example, StackView adds padding to its childrens' frames so as to
     * accomodate for the highlight effect.
     *
     * @return The FrameLayout into which children can be placed.
     */
    FrameLayout getFrameForChild() {
        return new FrameLayout(mContext);
    }

    void showOnly(int childIndex, boolean animate, boolean onLayout) {
        if (mAdapter == null) return;
        final int adapterCount = mAdapter.getCount();
        if (adapterCount == 0) return;

        for (int i = 0; i < mPreviousViews.size(); i++) {
            View viewToRemove = mViewsMap.get(mPreviousViews.get(i)).view;
            mViewsMap.remove(mPreviousViews.get(i));
            viewToRemove.clearAnimation();
            if (viewToRemove instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) viewToRemove;
                vg.removeAllViewsInLayout();
            }
            // applyTransformForChildAtIndex here just allows for any cleanup
            // associated with this view that may need to be done by a subclass
            applyTransformForChildAtIndex(viewToRemove, -1);

            removeViewInLayout(viewToRemove);
        }
        mPreviousViews.clear();
        int newWindowStartUnbounded = childIndex - mActiveOffset;
        int newWindowEndUnbounded = newWindowStartUnbounded + mNumActiveViews - 1;
        int newWindowStart = Math.max(0, newWindowStartUnbounded);
        int newWindowEnd = Math.min(adapterCount - 1, newWindowEndUnbounded);

        if (mLoopViews) {
            newWindowStart = newWindowStartUnbounded;
            newWindowEnd = newWindowEndUnbounded;
        }
        int rangeStart = modulo(newWindowStart, adapterCount);
        int rangeEnd = modulo(newWindowEnd, adapterCount);

        boolean wrap = false;
        if (rangeStart > rangeEnd) {
            wrap = true;
        }

        // This section clears out any items that are in our active views list
        // but are outside the effective bounds of our window (this is becomes an issue
        // at the extremities of the list, eg. where newWindowStartUnbounded < 0 or
        // newWindowEndUnbounded > mAdapter.getCount() - 1
        for (Integer index : mViewsMap.keySet()) {
            boolean remove = false;
            if (!wrap && (index < rangeStart || index > rangeEnd)) {
                remove = true;
            } else if (wrap && (index > rangeEnd && index < rangeStart)) {
                remove = true;
            }

            if (remove) {
                View previousView = mViewsMap.get(index).view;
                int oldRelativeIndex = mViewsMap.get(index).index;

                mPreviousViews.add(index);
                animateViewForTransition(oldRelativeIndex, -1, previousView);
            }
        }

        // If the window has changed
        if (!(newWindowStart == mCurrentWindowStart && newWindowEnd == mCurrentWindowEnd)) {
            // Run through the indices in the new range
            for (int i = newWindowStart; i <= newWindowEnd; i++) {

                int index = modulo(i, adapterCount);
                int oldRelativeIndex;
                if (mViewsMap.containsKey(index)) {
                    oldRelativeIndex = mViewsMap.get(index).index;
                } else {
                    oldRelativeIndex = -1;
                }
                int newRelativeIndex = i - newWindowStartUnbounded;

                // If this item is in the current window, great, we just need to apply
                // the transform for it's new relative position in the window, and animate
                // between it's current and new relative positions
                boolean inOldRange = mViewsMap.containsKey(index) && !mPreviousViews.contains(index);

                if (inOldRange) {
                    View view = mViewsMap.get(index).view;
                    mViewsMap.get(index).index = newRelativeIndex;
                    applyTransformForChildAtIndex(view, newRelativeIndex);
                    animateViewForTransition(oldRelativeIndex, newRelativeIndex, view);

                // Otherwise this view is new to the window
                } else {
                    // Get the new view from the adapter, add it and apply any transform / animation
                    View newView = mAdapter.getView(modulo(i, adapterCount), null, this);

                    // We wrap the new view in a FrameLayout so as to respect the contract
                    // with the adapter, that is, that we don't modify this view directly
                    FrameLayout fl = getFrameForChild();

                    // If the view from the adapter is null, we still keep an empty frame in place
                    if (newView != null) {
                       fl.addView(newView);
                    }
                    mViewsMap.put(index, new ViewAndIndex(fl, newRelativeIndex));
                    addChild(fl);
                    applyTransformForChildAtIndex(fl, newRelativeIndex);
                    animateViewForTransition(-1, newRelativeIndex, fl);
                }
                mViewsMap.get(index).view.bringToFront();
            }

            for (int i = 0; i < mViewsToBringToFront.size(); i++) {
                View v = mViewsToBringToFront.get(i);
                v.bringToFront();
            }
            mViewsToBringToFront.clear();

            mCurrentWindowStart = newWindowStart;
            mCurrentWindowEnd = newWindowEnd;
            mCurrentWindowStartUnbounded = newWindowStartUnbounded;
        }

        mFirstTime = false;
        if (!onLayout) {
            requestLayout();
            invalidate();
        } else {
            // If the Adapter tries to layout the current view when we get it using getView
            // above the layout will end up being ignored since we are currently laying out, so
            // we post a delayed requestLayout and invalidate
            mMainQueue.post(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                    invalidate();
                }
            });
        }
    }

    private void addChild(View child) {
        addViewInLayout(child, -1, createOrReuseLayoutParams(child));

        // This code is used to obtain a reference width and height of a child in case we need
        // to decide our own size. TODO: Do we want to update the size of the child that we're
        // using for reference size? If so, when?
        if (mReferenceChildWidth == -1 || mReferenceChildHeight == -1) {
            int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            child.measure(measureSpec, measureSpec);
            mReferenceChildWidth = child.getMeasuredWidth();
            mReferenceChildHeight = child.getMeasuredHeight();
        }
    }

    private void measureChildren() {
        final int count = getChildCount();
        final int childWidth = mMeasuredWidth - mPaddingLeft - mPaddingRight;
        final int childHeight = mMeasuredHeight - mPaddingTop - mPaddingBottom;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        boolean haveChildRefSize = (mReferenceChildWidth != -1 && mReferenceChildHeight != -1);

        // We need to deal with the case where our parent hasn't told us how
        // big we should be. In this case we try to use the desired size of the first
        // child added.
        if (heightSpecMode == MeasureSpec.UNSPECIFIED) {
            heightSpecSize = haveChildRefSize ? mReferenceChildHeight + mPaddingTop +
                    mPaddingBottom : 0;
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            heightSpecSize = haveChildRefSize ? Math.min(mReferenceChildHeight + mPaddingTop +
                    mPaddingBottom, heightSpecSize) : 0;
        }

        if (widthSpecMode == MeasureSpec.UNSPECIFIED) {
            widthSpecSize = haveChildRefSize ? mReferenceChildWidth + mPaddingLeft +
                    mPaddingRight : 0;
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            widthSpecSize = haveChildRefSize ? Math.min(mReferenceChildWidth + mPaddingLeft +
                    mPaddingRight, widthSpecSize) : 0;
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
        measureChildren();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        boolean dataChanged = mDataChanged;
        if (dataChanged) {
            handleDataChanged();

            // if the data changes, mWhichChild might be out of the bounds of the adapter
            // in this case, we reset mWhichChild to the beginning
            if (mWhichChild >= mAdapter.getCount()) {
                mWhichChild = 0;

                showOnly(mWhichChild, true, true);
            }
            refreshChildren();
        }

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            int childRight = mPaddingLeft + child.getMeasuredWidth();
            int childBottom = mPaddingTop + child.getMeasuredHeight();

            child.layout(mPaddingLeft, mPaddingTop, childRight, childBottom);
        }
        mDataChanged = false;
    }

    static class SavedState extends BaseSavedState {
        int whichChild;

        /**
         * Constructor called from {@link AdapterViewAnimator#onSaveInstanceState()}
         */
        SavedState(Parcelable superState, int whichChild) {
            super(superState);
            this.whichChild = whichChild;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            this.whichChild = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.whichChild);
        }

        @Override
        public String toString() {
            return "AdapterViewAnimator.SavedState{ whichChild = " + this.whichChild + " }";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mWhichChild);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        // Here we set mWhichChild in addition to setDisplayedChild
        // We do the former in case mAdapter is null, and hence setDisplayedChild won't
        // set mWhichChild
        mWhichChild = ss.whichChild;
        setDisplayedChild(mWhichChild);
    }

    /**
     * Shows only the specified child. The other displays Views exit the screen
     * with the {@link #getOutAnimation() out animation} and the specified child
     * enters the screen with the {@link #getInAnimation() in animation}.
     *
     * @param childIndex The index of the child to be shown.
     */
    void showOnly(int childIndex) {
        final boolean animate = (!mFirstTime || mAnimateFirstTime);
        showOnly(childIndex, animate);
    }

    /**
     * Returns the View corresponding to the currently displayed child.
     *
     * @return The View currently displayed.
     *
     * @see #getDisplayedChild()
     */
    public View getCurrentView() {
        return getViewAtRelativeIndex(mActiveOffset);
    }

    /**
     * Returns the current animation used to animate a View that enters the screen.
     *
     * @return An Animation or null if none is set.
     *
     * @see #setInAnimation(android.animation.ObjectAnimator)
     * @see #setInAnimation(android.content.Context, int)
     */
    public ObjectAnimator getInAnimation() {
        return mInAnimation;
    }

    /**
     * Specifies the animation used to animate a View that enters the screen.
     *
     * @param inAnimation The animation started when a View enters the screen.
     *
     * @see #getInAnimation()
     * @see #setInAnimation(android.content.Context, int)
     */
    public void setInAnimation(ObjectAnimator inAnimation) {
        mInAnimation = inAnimation;
    }

    /**
     * Returns the current animation used to animate a View that exits the screen.
     *
     * @return An Animation or null if none is set.
     *
     * @see #setOutAnimation(android.animation.ObjectAnimator)
     * @see #setOutAnimation(android.content.Context, int)
     */
    public ObjectAnimator getOutAnimation() {
        return mOutAnimation;
    }

    /**
     * Specifies the animation used to animate a View that exit the screen.
     *
     * @param outAnimation The animation started when a View exit the screen.
     *
     * @see #getOutAnimation()
     * @see #setOutAnimation(android.content.Context, int)
     */
    public void setOutAnimation(ObjectAnimator outAnimation) {
        mOutAnimation = outAnimation;
    }

    /**
     * Specifies the animation used to animate a View that enters the screen.
     *
     * @param context The application's environment.
     * @param resourceID The resource id of the animation.
     *
     * @see #getInAnimation()
     * @see #setInAnimation(android.animation.ObjectAnimator)
     */
    public void setInAnimation(Context context, int resourceID) {
        setInAnimation((ObjectAnimator) AnimatorInflater.loadAnimator(context, resourceID));
    }

    /**
     * Specifies the animation used to animate a View that exit the screen.
     *
     * @param context The application's environment.
     * @param resourceID The resource id of the animation.
     *
     * @see #getOutAnimation()
     * @see #setOutAnimation(android.animation.ObjectAnimator)
     */
    public void setOutAnimation(Context context, int resourceID) {
        setOutAnimation((ObjectAnimator) AnimatorInflater.loadAnimator(context, resourceID));
    }

    /**
     * Indicates whether the current View should be animated the first time
     * the ViewAnimation is displayed.
     *
     * @param animate True to animate the current View the first time it is displayed,
     *                false otherwise.
     */
    public void setAnimateFirstView(boolean animate) {
        mAnimateFirstTime = animate;
    }

    @Override
    public int getBaseline() {
        return (getCurrentView() != null) ? getCurrentView().getBaseline() : super.getBaseline();
    }

    @Override
    public Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        mAdapter = adapter;
        checkFocus();

        if (mAdapter != null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
        setFocusable(true);
    }

    /**
     * Sets up this AdapterViewAnimator to use a remote views adapter which connects to a
     * RemoteViewsService through the specified intent.
     *
     * @param intent the intent used to identify the RemoteViewsService for the adapter to
     *        connect to.
     */
    @android.view.RemotableViewMethod
    public void setRemoteViewsAdapter(Intent intent) {
        // Ensure that we don't already have a RemoteViewsAdapter that is bound to an existing
        // service handling the specified intent.
        if (mRemoteViewsAdapter != null) {
            Intent.FilterComparison fcNew = new Intent.FilterComparison(intent);
            Intent.FilterComparison fcOld = new Intent.FilterComparison(
                    mRemoteViewsAdapter.getRemoteViewsServiceIntent());
            if (fcNew.equals(fcOld)) {
                return;
            }
        }

        // Otherwise, create a new RemoteViewsAdapter for binding
        mRemoteViewsAdapter = new RemoteViewsAdapter(getContext(), intent, this);
    }

    @Override
    public void setSelection(int position) {
        setDisplayedChild(position);
    }

    @Override
    public View getSelectedView() {
        return getViewAtRelativeIndex(mActiveOffset);
    }

    /**
     * Called back when the adapter connects to the RemoteViewsService.
     */
    public void onRemoteAdapterConnected() {
        if (mRemoteViewsAdapter != mAdapter) {
            setAdapter(mRemoteViewsAdapter);
        }
    }

    /**
     * Called back when the adapter disconnects from the RemoteViewsService.
     */
    public void onRemoteAdapterDisconnected() {
        if (mRemoteViewsAdapter != mAdapter) {
            mRemoteViewsAdapter = null;
            setAdapter(mRemoteViewsAdapter);
        }
    }
}