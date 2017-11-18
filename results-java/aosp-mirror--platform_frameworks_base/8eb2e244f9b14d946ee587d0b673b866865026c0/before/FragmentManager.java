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

package android.app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Interface for interacting with {@link Fragment} objects inside of an
 * {@link Activity}
 */
public interface FragmentManager {
    /**
     * Representation of an entry on the fragment back stack, as created
     * with {@link FragmentTransaction#addToBackStack(String)
     * FragmentTransaction.addToBackStack()}.  Entries can later be
     * retrieved with {@link FragmentManager#getBackStackEntry(int)
     * FragmentManager.getBackStackEntry()}.
     *
     * <p>Note that you should never hold on to a BackStackEntry object;
     * the identifier as returned by {@link #getId} is the only thing that
     * will be persisted across activity instances.
     */
    public interface BackStackEntry {
        /**
         * Return the unique identifier for the entry.  This is the only
         * representation of the entry that will persist across activity
         * instances.
         */
        public int getId();

        /**
         * Return the full bread crumb title for the entry, or null if it
         * does not have one.
         */
        public CharSequence getBreadCrumbTitle();

        /**
         * Return the short bread crumb title for the entry, or null if it
         * does not have one.
         */
        public CharSequence getBreadCrumbShortTitle();
    }

    /**
     * Interface to watch for changes to the back stack.
     */
    public interface OnBackStackChangedListener {
        /**
         * Called whenever the contents of the back stack change.
         */
        public void onBackStackChanged();
    }

    /**
     * Start a series of edit operations on the Fragments associated with
     * this FragmentManager.
     *
     * <p>Note: A fragment transaction can only be created/committed prior
     * to an activity saving its state.  If you try to commit a transaction
     * after {@link Activity#onSaveInstanceState Activity.onSaveInstanceState()}
     * (and prior to a following {@link Activity#onStart Activity.onStart}
     * or {@link Activity#onResume Activity.onResume()}, you will get an error.
     * This is because the framework takes care of saving your current fragments
     * in the state, and if changes are made after the state is saved then they
     * will be lost.</p>
     */
    public FragmentTransaction openTransaction();

    /**
     * Finds a fragment that was identified by the given id either when inflated
     * from XML or as the container ID when added in a transaction.  This first
     * searches through fragments that are currently added to the manager's
     * activity; if no such fragment is found, then all fragments currently
     * on the back stack associated with this ID are searched.
     * @return The fragment if found or null otherwise.
     */
    public Fragment findFragmentById(int id);

    /**
     * Finds a fragment that was identified by the given tag either when inflated
     * from XML or as supplied when added in a transaction.  This first
     * searches through fragments that are currently added to the manager's
     * activity; if no such fragment is found, then all fragments currently
     * on the back stack are searched.
     * @return The fragment if found or null otherwise.
     */
    public Fragment findFragmentByTag(String tag);

    /**
     * Flag for {@link #popBackStack(String, int)}
     * and {@link #popBackStack(int, int)}: If set, and the name or ID of
     * a back stack entry has been supplied, then all matching entries will
     * be consumed until one that doesn't match is found or the bottom of
     * the stack is reached.  Otherwise, all entries up to but not including that entry
     * will be removed.
     */
    public static final int POP_BACK_STACK_INCLUSIVE = 1<<0;

    /**
     * Pop the top state off the back stack.  Returns true if there was one
     * to pop, else false.
     */
    public boolean popBackStack();

    /**
     * Pop the last fragment transition from the manager's fragment
     * back stack.  If there is nothing to pop, false is returned.
     * @param name If non-null, this is the name of a previous back state
     * to look for; if found, all states up to that state will be popped.  The
     * {@link #POP_BACK_STACK_INCLUSIVE} flag can be used to control whether
     * the named state itself is popped. If null, only the top state is popped.
     * @param flags Either 0 or {@link #POP_BACK_STACK_INCLUSIVE}.
     */
    public boolean popBackStack(String name, int flags);

    /**
     * Pop all back stack states up to the one with the given identifier.
     * @param id Identifier of the stated to be popped. If no identifier exists,
     * false is returned.
     * The identifier is the number returned by
     * {@link FragmentTransaction#commit() FragmentTransaction.commit()}.  The
     * {@link #POP_BACK_STACK_INCLUSIVE} flag can be used to control whether
     * the named state itself is popped.
     * @param flags Either 0 or {@link #POP_BACK_STACK_INCLUSIVE}.
     */
    public boolean popBackStack(int id, int flags);

    /**
     * Return the number of entries currently in the back stack.
     */
    public int countBackStackEntries();

    /**
     * Return the BackStackEntry at index <var>index</var> in the back stack;
     * entries start index 0 being the bottom of the stack.
     */
    public BackStackEntry getBackStackEntry(int index);

    /**
     * Add a new listener for changes to the fragment back stack.
     */
    public void addOnBackStackChangedListener(OnBackStackChangedListener listener);

    /**
     * Remove a listener that was previously added with
     * {@link #addOnBackStackChangedListener(OnBackStackChangedListener)}.
     */
    public void removeOnBackStackChangedListener(OnBackStackChangedListener listener);

    /**
     * Put a reference to a fragment in a Bundle.  This Bundle can be
     * persisted as saved state, and when later restoring
     * {@link #getFragment(Bundle, String)} will return the current
     * instance of the same fragment.
     *
     * @param bundle The bundle in which to put the fragment reference.
     * @param key The name of the entry in the bundle.
     * @param fragment The Fragment whose reference is to be stored.
     */
    public void putFragment(Bundle bundle, String key, Fragment fragment);

    /**
     * Retrieve the current Fragment instance for a reference previously
     * placed with {@link #putFragment(Bundle, String, Fragment)}.
     *
     * @param bundle The bundle from which to retrieve the fragment reference.
     * @param key The name of the entry in the bundle.
     * @return Returns the current Fragment instance that is associated with
     * the given reference.
     */
    public Fragment getFragment(Bundle bundle, String key);

    /**
     * Print the FragmentManager's state into the given stream.
     *
     * @param prefix Text to print at the front of each line.
     * @param fd The raw file descriptor that the dump is being sent to.
     * @param writer A PrintWriter to which the dump is to be set.
     * @param args additional arguments to the dump request.
     */
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args);
}

final class FragmentManagerState implements Parcelable {
    FragmentState[] mActive;
    int[] mAdded;
    BackStackState[] mBackStack;

    public FragmentManagerState() {
    }

    public FragmentManagerState(Parcel in) {
        mActive = in.createTypedArray(FragmentState.CREATOR);
        mAdded = in.createIntArray();
        mBackStack = in.createTypedArray(BackStackState.CREATOR);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(mActive, flags);
        dest.writeIntArray(mAdded);
        dest.writeTypedArray(mBackStack, flags);
    }

    public static final Parcelable.Creator<FragmentManagerState> CREATOR
            = new Parcelable.Creator<FragmentManagerState>() {
        public FragmentManagerState createFromParcel(Parcel in) {
            return new FragmentManagerState(in);
        }

        public FragmentManagerState[] newArray(int size) {
            return new FragmentManagerState[size];
        }
    };
}

/**
 * Container for fragments associated with an activity.
 */
final class FragmentManagerImpl implements FragmentManager {
    static final boolean DEBUG = true;
    static final String TAG = "FragmentManager";

    static final String TARGET_REQUEST_CODE_STATE_TAG = "android:target_req_state";
    static final String TARGET_STATE_TAG = "android:target_state";
    static final String VIEW_STATE_TAG = "android:view_state";

    ArrayList<Runnable> mPendingActions;
    Runnable[] mTmpActions;
    boolean mExecutingActions;

    ArrayList<Fragment> mActive;
    ArrayList<Fragment> mAdded;
    ArrayList<Integer> mAvailIndices;
    ArrayList<BackStackRecord> mBackStack;

    // Must be accessed while locked.
    ArrayList<BackStackRecord> mBackStackIndices;
    ArrayList<Integer> mAvailBackStackIndices;

    ArrayList<OnBackStackChangedListener> mBackStackChangeListeners;

    int mCurState = Fragment.INITIALIZING;
    Activity mActivity;

    boolean mNeedMenuInvalidate;
    boolean mStateSaved;
    String mNoTransactionsBecause;

    // Temporary vars for state save and restore.
    Bundle mStateBundle = null;
    SparseArray<Parcelable> mStateArray = null;

    Runnable mExecCommit = new Runnable() {
        @Override
        public void run() {
            execPendingActions();
        }
    };

    @Override
    public FragmentTransaction openTransaction() {
        return new BackStackRecord(this);
    }

    @Override
    public boolean popBackStack() {
        return popBackStackState(mActivity.mHandler, null, -1, 0);
    }

    @Override
    public boolean popBackStack(String name, int flags) {
        return popBackStackState(mActivity.mHandler, name, -1, flags);
    }

    @Override
    public boolean popBackStack(int id, int flags) {
        if (id < 0) {
            throw new IllegalArgumentException("Bad id: " + id);
        }
        return popBackStackState(mActivity.mHandler, null, id, flags);
    }

    @Override
    public int countBackStackEntries() {
        return mBackStack != null ? mBackStack.size() : 0;
    }

    @Override
    public BackStackEntry getBackStackEntry(int index) {
        return mBackStack.get(index);
    }

    @Override
    public void addOnBackStackChangedListener(OnBackStackChangedListener listener) {
        if (mBackStackChangeListeners == null) {
            mBackStackChangeListeners = new ArrayList<OnBackStackChangedListener>();
        }
        mBackStackChangeListeners.add(listener);
    }

    @Override
    public void removeOnBackStackChangedListener(OnBackStackChangedListener listener) {
        if (mBackStackChangeListeners != null) {
            mBackStackChangeListeners.remove(listener);
        }
    }

    @Override
    public void putFragment(Bundle bundle, String key, Fragment fragment) {
        if (fragment.mIndex < 0) {
            throw new IllegalStateException("Fragment " + fragment
                    + " is not currently in the FragmentManager");
        }
        bundle.putInt(key, fragment.mIndex);
    }

    @Override
    public Fragment getFragment(Bundle bundle, String key) {
        int index = bundle.getInt(key, -1);
        if (index == -1) {
            return null;
        }
        if (index >= mActive.size()) {
            throw new IllegalStateException("Fragement no longer exists for key "
                    + key + ": index " + index);
        }
        Fragment f = mActive.get(index);
        if (f == null) {
            throw new IllegalStateException("Fragement no longer exists for key "
                    + key + ": index " + index);
        }
        return f;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        if (mActive == null || mActive.size() <= 0) {
            return;
        }

        writer.print(prefix); writer.println("Active Fragments:");

        String innerPrefix = prefix + "    ";

        int N = mActive.size();
        for (int i=0; i<N; i++) {
            Fragment f = mActive.get(i);
            if (f != null) {
                writer.print(prefix); writer.print("  #"); writer.print(i);
                        writer.print(": "); writer.println(f.toString());
                f.dump(innerPrefix, fd, writer, args);
            }
        }

        if (mAdded != null) {
            N = mAdded.size();
            if (N > 0) {
                writer.print(prefix); writer.println("Added Fragments:");
                for (int i=0; i<N; i++) {
                    Fragment f = mAdded.get(i);
                    writer.print(prefix); writer.print("  #"); writer.print(i);
                            writer.print(": "); writer.println(f.toString());
                }
            }
        }

        if (mBackStack != null) {
            N = mBackStack.size();
            if (N > 0) {
                writer.print(prefix); writer.println("Back Stack:");
                for (int i=0; i<N; i++) {
                    BackStackRecord bs = mBackStack.get(i);
                    writer.print(prefix); writer.print("  #"); writer.print(i);
                            writer.print(": "); writer.println(bs.toString());
                }
            }
        }
    }

    Animator loadAnimator(Fragment fragment, int transit, boolean enter,
            int transitionStyle) {
        Animator animObj = fragment.onCreateAnimator(transit, enter,
                fragment.mNextAnim);
        if (animObj != null) {
            return animObj;
        }

        if (fragment.mNextAnim != 0) {
            Animator anim = AnimatorInflater.loadAnimator(mActivity, fragment.mNextAnim);
            if (anim != null) {
                return anim;
            }
        }

        if (transit == 0) {
            return null;
        }

        int styleIndex = transitToStyleIndex(transit, enter);
        if (styleIndex < 0) {
            return null;
        }

        if (transitionStyle == 0 && mActivity.getWindow() != null) {
            transitionStyle = mActivity.getWindow().getAttributes().windowAnimations;
        }
        if (transitionStyle == 0) {
            return null;
        }

        TypedArray attrs = mActivity.obtainStyledAttributes(transitionStyle,
                com.android.internal.R.styleable.FragmentAnimation);
        int anim = attrs.getResourceId(styleIndex, 0);
        attrs.recycle();

        if (anim == 0) {
            return null;
        }

        return AnimatorInflater.loadAnimator(mActivity, anim);
    }

    void moveToState(Fragment f, int newState, int transit, int transitionStyle) {
        // Fragments that are not currently added will sit in the onCreate() state.
        if (!f.mAdded && newState > Fragment.CREATED) {
            newState = Fragment.CREATED;
        }

        if (f.mState < newState) {
            switch (f.mState) {
                case Fragment.INITIALIZING:
                    if (DEBUG) Log.v(TAG, "moveto CREATED: " + f);
                    if (f.mSavedFragmentState != null) {
                        f.mSavedViewState = f.mSavedFragmentState.getSparseParcelableArray(
                                FragmentManagerImpl.VIEW_STATE_TAG);
                        f.mTarget = getFragment(f.mSavedFragmentState,
                                FragmentManagerImpl.TARGET_STATE_TAG);
                        if (f.mTarget != null) {
                            f.mTargetRequestCode = f.mSavedFragmentState.getInt(
                                    FragmentManagerImpl.TARGET_REQUEST_CODE_STATE_TAG, 0);
                        }
                    }
                    f.mActivity = mActivity;
                    f.mCalled = false;
                    f.onAttach(mActivity);
                    if (!f.mCalled) {
                        throw new SuperNotCalledException("Fragment " + f
                                + " did not call through to super.onAttach()");
                    }
                    mActivity.onAttachFragment(f);

                    if (!f.mRetaining) {
                        f.mCalled = false;
                        f.onCreate(f.mSavedFragmentState);
                        if (!f.mCalled) {
                            throw new SuperNotCalledException("Fragment " + f
                                    + " did not call through to super.onCreate()");
                        }
                    }
                    f.mRetaining = false;
                    if (f.mFromLayout) {
                        // For fragments that are part of the content view
                        // layout, we need to instantiate the view immediately
                        // and the inflater will take care of adding it.
                        f.mView = f.onCreateView(mActivity.getLayoutInflater(),
                                null, f.mSavedFragmentState);
                        if (f.mView != null) {
                            f.mView.setSaveFromParentEnabled(false);
                            f.restoreViewState();
                            if (f.mHidden) f.mView.setVisibility(View.GONE);
                        }
                    }
                case Fragment.CREATED:
                    if (newState > Fragment.CREATED) {
                        if (DEBUG) Log.v(TAG, "moveto CONTENT: " + f);
                        if (!f.mFromLayout) {
                            ViewGroup container = null;
                            if (f.mContainerId != 0) {
                                container = (ViewGroup)mActivity.findViewById(f.mContainerId);
                                if (container == null) {
                                    throw new IllegalArgumentException("No view found for id 0x"
                                            + Integer.toHexString(f.mContainerId)
                                            + " for fragment " + f);
                                }
                            }
                            f.mContainer = container;
                            f.mView = f.onCreateView(mActivity.getLayoutInflater(),
                                    container, f.mSavedFragmentState);
                            if (f.mView != null) {
                                f.mView.setSaveFromParentEnabled(false);
                                if (container != null) {
                                    Animator anim = loadAnimator(f, transit, true,
                                            transitionStyle);
                                    if (anim != null) {
                                        anim.setTarget(f.mView);
                                        anim.start();
                                    }
                                    container.addView(f.mView);
                                    f.restoreViewState();
                                }
                                if (f.mHidden) f.mView.setVisibility(View.GONE);
                            }
                        }

                        f.mCalled = false;
                        f.onActivityCreated(f.mSavedFragmentState);
                        if (!f.mCalled) {
                            throw new SuperNotCalledException("Fragment " + f
                                    + " did not call through to super.onReady()");
                        }
                        f.mSavedFragmentState = null;
                    }
                case Fragment.ACTIVITY_CREATED:
                    if (newState > Fragment.ACTIVITY_CREATED) {
                        if (DEBUG) Log.v(TAG, "moveto STARTED: " + f);
                        f.mCalled = false;
                        f.onStart();
                        if (!f.mCalled) {
                            throw new SuperNotCalledException("Fragment " + f
                                    + " did not call through to super.onStart()");
                        }
                    }
                case Fragment.STARTED:
                    if (newState > Fragment.STARTED) {
                        if (DEBUG) Log.v(TAG, "moveto RESUMED: " + f);
                        f.mCalled = false;
                        f.mResumed = true;
                        f.onResume();
                        if (!f.mCalled) {
                            throw new SuperNotCalledException("Fragment " + f
                                    + " did not call through to super.onResume()");
                        }
                    }
            }
        } else if (f.mState > newState) {
            switch (f.mState) {
                case Fragment.RESUMED:
                    if (newState < Fragment.RESUMED) {
                        if (DEBUG) Log.v(TAG, "movefrom RESUMED: " + f);
                        f.mCalled = false;
                        f.onPause();
                        if (!f.mCalled) {
                            throw new SuperNotCalledException("Fragment " + f
                                    + " did not call through to super.onPause()");
                        }
                        f.mResumed = false;
                    }
                case Fragment.STARTED:
                    if (newState < Fragment.STARTED) {
                        if (DEBUG) Log.v(TAG, "movefrom STARTED: " + f);
                        f.mCalled = false;
                        f.performStop();
                        if (!f.mCalled) {
                            throw new SuperNotCalledException("Fragment " + f
                                    + " did not call through to super.onStop()");
                        }
                    }
                case Fragment.ACTIVITY_CREATED:
                    if (newState < Fragment.ACTIVITY_CREATED) {
                        if (DEBUG) Log.v(TAG, "movefrom CONTENT: " + f);
                        if (f.mView != null) {
                            // Need to save the current view state if not
                            // done already.
                            if (!mActivity.isFinishing() && f.mSavedViewState == null) {
                                saveFragmentViewState(f);
                            }
                        }
                        f.mCalled = false;
                        f.onDestroyView();
                        if (!f.mCalled) {
                            throw new SuperNotCalledException("Fragment " + f
                                    + " did not call through to super.onDestroyedView()");
                        }
                        if (f.mView != null && f.mContainer != null) {
                            Animator anim = null;
                            if (mCurState > Fragment.INITIALIZING) {
                                anim = loadAnimator(f, transit, false,
                                        transitionStyle);
                            }
                            if (anim != null) {
                                final ViewGroup container = f.mContainer;
                                final View view = f.mView;
                                container.startViewTransition(view);
                                anim.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator anim) {
                                        container.endViewTransition(view);
                                    }
                                });
                                anim.setTarget(f.mView);
                                anim.start();

                            }
                            f.mContainer.removeView(f.mView);
                        }
                        f.mContainer = null;
                        f.mView = null;
                    }
                case Fragment.CREATED:
                    if (newState < Fragment.CREATED) {
                        if (DEBUG) Log.v(TAG, "movefrom CREATED: " + f);
                        if (!f.mRetaining) {
                            f.mCalled = false;
                            f.onDestroy();
                            if (!f.mCalled) {
                                throw new SuperNotCalledException("Fragment " + f
                                        + " did not call through to super.onDestroy()");
                            }
                        }

                        f.mCalled = false;
                        f.onDetach();
                        if (!f.mCalled) {
                            throw new SuperNotCalledException("Fragment " + f
                                    + " did not call through to super.onDetach()");
                        }
                        f.mImmediateActivity = null;
                        f.mActivity = null;
                    }
            }
        }

        f.mState = newState;
    }

    void moveToState(Fragment f) {
        moveToState(f, mCurState, 0, 0);
    }

    void moveToState(int newState, boolean always) {
        moveToState(newState, 0, 0, always);
    }

    void moveToState(int newState, int transit, int transitStyle, boolean always) {
        if (mActivity == null && newState != Fragment.INITIALIZING) {
            throw new IllegalStateException("No activity");
        }

        if (!always && mCurState == newState) {
            return;
        }

        mCurState = newState;
        if (mActive != null) {
            for (int i=0; i<mActive.size(); i++) {
                Fragment f = mActive.get(i);
                if (f != null) {
                    moveToState(f, newState, transit, transitStyle);
                }
            }

            if (mNeedMenuInvalidate && mActivity != null) {
                mActivity.invalidateOptionsMenu();
                mNeedMenuInvalidate = false;
            }
        }
    }

    void makeActive(Fragment f) {
        if (f.mIndex >= 0) {
            return;
        }

        if (mAvailIndices == null || mAvailIndices.size() <= 0) {
            if (mActive == null) {
                mActive = new ArrayList<Fragment>();
            }
            f.setIndex(mActive.size());
            mActive.add(f);

        } else {
            f.setIndex(mAvailIndices.remove(mAvailIndices.size()-1));
            mActive.set(f.mIndex, f);
        }
    }

    void makeInactive(Fragment f) {
        if (f.mIndex < 0) {
            return;
        }

        if (DEBUG) Log.v(TAG, "Freeing fragment index " + f.mIndex);
        mActive.set(f.mIndex, null);
        if (mAvailIndices == null) {
            mAvailIndices = new ArrayList<Integer>();
        }
        mAvailIndices.add(f.mIndex);
        mActivity.invalidateFragmentIndex(f.mIndex);
        f.clearIndex();
    }

    public void addFragment(Fragment fragment, boolean moveToStateNow) {
        if (mAdded == null) {
            mAdded = new ArrayList<Fragment>();
        }
        mAdded.add(fragment);
        makeActive(fragment);
        if (DEBUG) Log.v(TAG, "add: " + fragment);
        fragment.mAdded = true;
        if (fragment.mHasMenu) {
            mNeedMenuInvalidate = true;
        }
        if (moveToStateNow) {
            moveToState(fragment);
        }
    }

    public void removeFragment(Fragment fragment, int transition, int transitionStyle) {
        if (DEBUG) Log.v(TAG, "remove: " + fragment + " nesting=" + fragment.mBackStackNesting);
        mAdded.remove(fragment);
        final boolean inactive = fragment.mBackStackNesting <= 0;
        if (fragment.mHasMenu) {
            mNeedMenuInvalidate = true;
        }
        fragment.mAdded = false;
        moveToState(fragment, inactive ? Fragment.INITIALIZING : Fragment.CREATED,
                transition, transitionStyle);
        if (inactive) {
            makeInactive(fragment);
        }
    }

    public void hideFragment(Fragment fragment, int transition, int transitionStyle) {
        if (DEBUG) Log.v(TAG, "hide: " + fragment);
        if (!fragment.mHidden) {
            fragment.mHidden = true;
            if (fragment.mView != null) {
                Animator anim = loadAnimator(fragment, transition, true,
                        transitionStyle);
                if (anim != null) {
                    anim.setTarget(fragment.mView);
                    anim.start();
                }
                fragment.mView.setVisibility(View.GONE);
            }
            if (fragment.mAdded && fragment.mHasMenu) {
                mNeedMenuInvalidate = true;
            }
            fragment.onHiddenChanged(true);
        }
    }

    public void showFragment(Fragment fragment, int transition, int transitionStyle) {
        if (DEBUG) Log.v(TAG, "show: " + fragment);
        if (fragment.mHidden) {
            fragment.mHidden = false;
            if (fragment.mView != null) {
                Animator anim = loadAnimator(fragment, transition, true,
                        transitionStyle);
                if (anim != null) {
                    anim.setTarget(fragment.mView);
                    anim.start();
                }
                fragment.mView.setVisibility(View.VISIBLE);
            }
            if (fragment.mAdded && fragment.mHasMenu) {
                mNeedMenuInvalidate = true;
            }
            fragment.onHiddenChanged(false);
        }
    }

    public Fragment findFragmentById(int id) {
        if (mActive != null) {
            // First look through added fragments.
            for (int i=mAdded.size()-1; i>=0; i--) {
                Fragment f = mAdded.get(i);
                if (f != null && f.mFragmentId == id) {
                    return f;
                }
            }
            // Now for any known fragment.
            for (int i=mActive.size()-1; i>=0; i--) {
                Fragment f = mActive.get(i);
                if (f != null && f.mFragmentId == id) {
                    return f;
                }
            }
        }
        return null;
    }

    public Fragment findFragmentByTag(String tag) {
        if (mActive != null && tag != null) {
            // First look through added fragments.
            for (int i=mAdded.size()-1; i>=0; i--) {
                Fragment f = mAdded.get(i);
                if (f != null && tag.equals(f.mTag)) {
                    return f;
                }
            }
            // Now for any known fragment.
            for (int i=mActive.size()-1; i>=0; i--) {
                Fragment f = mActive.get(i);
                if (f != null && tag.equals(f.mTag)) {
                    return f;
                }
            }
        }
        return null;
    }

    public Fragment findFragmentByWho(String who) {
        if (mActive != null && who != null) {
            for (int i=mActive.size()-1; i>=0; i--) {
                Fragment f = mActive.get(i);
                if (f != null && who.equals(f.mWho)) {
                    return f;
                }
            }
        }
        return null;
    }

    public void enqueueAction(Runnable action) {
        if (mStateSaved) {
            throw new IllegalStateException(
                    "Can not perform this action after onSaveInstanceState");
        }
        if (mNoTransactionsBecause != null) {
            throw new IllegalStateException(
                    "Can not perform this action inside of " + mNoTransactionsBecause);
        }
        synchronized (this) {
            if (mPendingActions == null) {
                mPendingActions = new ArrayList<Runnable>();
            }
            mPendingActions.add(action);
            if (mPendingActions.size() == 1) {
                mActivity.mHandler.removeCallbacks(mExecCommit);
                mActivity.mHandler.post(mExecCommit);
            }
        }
    }

    public int allocBackStackIndex(BackStackRecord bse) {
        synchronized (this) {
            if (mAvailBackStackIndices == null || mAvailBackStackIndices.size() <= 0) {
                if (mBackStackIndices == null) {
                    mBackStackIndices = new ArrayList<BackStackRecord>();
                }
                int index = mBackStackIndices.size();
                if (DEBUG) Log.v(TAG, "Setting back stack index " + index + " to " + bse);
                mBackStackIndices.add(bse);
                return index;

            } else {
                int index = mAvailBackStackIndices.remove(mAvailBackStackIndices.size()-1);
                if (DEBUG) Log.v(TAG, "Adding back stack index " + index + " with " + bse);
                mBackStackIndices.set(index, bse);
                return index;
            }
        }
    }

    public void setBackStackIndex(int index, BackStackRecord bse) {
        synchronized (this) {
            if (mBackStackIndices == null) {
                mBackStackIndices = new ArrayList<BackStackRecord>();
            }
            int N = mBackStackIndices.size();
            if (index < N) {
                if (DEBUG) Log.v(TAG, "Setting back stack index " + index + " to " + bse);
                mBackStackIndices.set(index, bse);
            } else {
                while (N < index) {
                    mBackStackIndices.add(null);
                    if (mAvailBackStackIndices == null) {
                        mAvailBackStackIndices = new ArrayList<Integer>();
                    }
                    if (DEBUG) Log.v(TAG, "Adding available back stack index " + N);
                    mAvailBackStackIndices.add(N);
                    N++;
                }
                if (DEBUG) Log.v(TAG, "Adding back stack index " + index + " with " + bse);
                mBackStackIndices.add(bse);
            }
        }
    }

    public void freeBackStackIndex(int index) {
        synchronized (this) {
            mBackStackIndices.set(index, null);
            if (mAvailBackStackIndices == null) {
                mAvailBackStackIndices = new ArrayList<Integer>();
            }
            if (DEBUG) Log.v(TAG, "Freeing back stack index " + index);
            mAvailBackStackIndices.add(index);
        }
    }

    /**
     * Only call from main thread!
     */
    public void execPendingActions() {
        if (mExecutingActions) {
            throw new IllegalStateException("Recursive entry to execPendingActions");
        }

        while (true) {
            int numActions;

            synchronized (this) {
                if (mPendingActions == null || mPendingActions.size() == 0) {
                    return;
                }

                numActions = mPendingActions.size();
                if (mTmpActions == null || mTmpActions.length < numActions) {
                    mTmpActions = new Runnable[numActions];
                }
                mPendingActions.toArray(mTmpActions);
                mPendingActions.clear();
                mActivity.mHandler.removeCallbacks(mExecCommit);
            }

            mExecutingActions = true;
            for (int i=0; i<numActions; i++) {
                mTmpActions[i].run();
            }
            mExecutingActions = false;
        }
    }

    void reportBackStackChanged() {
        if (mBackStackChangeListeners != null) {
            for (int i=0; i<mBackStackChangeListeners.size(); i++) {
                mBackStackChangeListeners.get(i).onBackStackChanged();
            }
        }
    }

    void addBackStackState(BackStackRecord state) {
        if (mBackStack == null) {
            mBackStack = new ArrayList<BackStackRecord>();
        }
        mBackStack.add(state);
        reportBackStackChanged();
    }

    boolean popBackStackState(Handler handler, String name, int id, int flags) {
        if (mBackStack == null) {
            return false;
        }
        if (name == null && id < 0 && (flags&Activity.POP_BACK_STACK_INCLUSIVE) == 0) {
            int last = mBackStack.size()-1;
            if (last < 0) {
                return false;
            }
            final BackStackRecord bss = mBackStack.remove(last);
            enqueueAction(new Runnable() {
                public void run() {
                    if (DEBUG) Log.v(TAG, "Popping back stack state: " + bss);
                    bss.popFromBackStack(true);
                    reportBackStackChanged();
                }
            });
        } else {
            int index = -1;
            if (name != null || id >= 0) {
                // If a name or ID is specified, look for that place in
                // the stack.
                index = mBackStack.size()-1;
                while (index >= 0) {
                    BackStackRecord bss = mBackStack.get(index);
                    if (name != null && name.equals(bss.getName())) {
                        break;
                    }
                    if (id >= 0 && id == bss.mIndex) {
                        break;
                    }
                    index--;
                }
                if (index < 0) {
                    return false;
                }
                if ((flags&Activity.POP_BACK_STACK_INCLUSIVE) != 0) {
                    index--;
                    // Consume all following entries that match.
                    while (index >= 0) {
                        BackStackRecord bss = mBackStack.get(index);
                        if ((name != null && name.equals(bss.getName()))
                                || (id >= 0 && id == bss.mIndex)) {
                            index--;
                            continue;
                        }
                        break;
                    }
                }
            }
            if (index == mBackStack.size()-1) {
                return false;
            }
            final ArrayList<BackStackRecord> states
                    = new ArrayList<BackStackRecord>();
            for (int i=mBackStack.size()-1; i>index; i--) {
                states.add(mBackStack.remove(i));
            }
            enqueueAction(new Runnable() {
                public void run() {
                    final int LAST = states.size()-1;
                    for (int i=0; i<=LAST; i++) {
                        if (DEBUG) Log.v(TAG, "Popping back stack state: " + states.get(i));
                        states.get(i).popFromBackStack(i == LAST);
                    }
                    reportBackStackChanged();
                }
            });
        }
        return true;
    }

    ArrayList<Fragment> retainNonConfig() {
        ArrayList<Fragment> fragments = null;
        if (mActive != null) {
            for (int i=0; i<mActive.size(); i++) {
                Fragment f = mActive.get(i);
                if (f != null && f.mRetainInstance) {
                    if (fragments == null) {
                        fragments = new ArrayList<Fragment>();
                    }
                    fragments.add(f);
                    f.mRetaining = true;
                }
            }
        }
        return fragments;
    }

    void saveFragmentViewState(Fragment f) {
        if (f.mView == null) {
            return;
        }
        if (mStateArray == null) {
            mStateArray = new SparseArray<Parcelable>();
        }
        f.mView.saveHierarchyState(mStateArray);
        if (mStateArray.size() > 0) {
            f.mSavedViewState = mStateArray;
            mStateArray = null;
        }
    }

    Parcelable saveAllState() {
        mStateSaved = true;

        if (mActive == null || mActive.size() <= 0) {
            return null;
        }

        // First collect all active fragments.
        int N = mActive.size();
        FragmentState[] active = new FragmentState[N];
        boolean haveFragments = false;
        for (int i=0; i<N; i++) {
            Fragment f = mActive.get(i);
            if (f != null) {
                haveFragments = true;

                FragmentState fs = new FragmentState(f);
                active[i] = fs;

                if (f.mState > Fragment.INITIALIZING && fs.mSavedFragmentState == null) {
                    if (mStateBundle == null) {
                        mStateBundle = new Bundle();
                    }
                    f.onSaveInstanceState(mStateBundle);
                    if (!mStateBundle.isEmpty()) {
                        fs.mSavedFragmentState = mStateBundle;
                        mStateBundle = null;
                    }

                    if (f.mView != null) {
                        saveFragmentViewState(f);
                        if (f.mSavedViewState != null) {
                            if (fs.mSavedFragmentState == null) {
                                fs.mSavedFragmentState = new Bundle();
                            }
                            fs.mSavedFragmentState.putSparseParcelableArray(
                                    FragmentManagerImpl.VIEW_STATE_TAG, f.mSavedViewState);
                        }
                    }

                    if (f.mTarget != null) {
                        if (fs.mSavedFragmentState == null) {
                            fs.mSavedFragmentState = new Bundle();
                        }
                        putFragment(fs.mSavedFragmentState,
                                FragmentManagerImpl.TARGET_STATE_TAG, f.mTarget);
                        if (f.mTargetRequestCode != 0) {
                            fs.mSavedFragmentState.putInt(
                                    FragmentManagerImpl.TARGET_REQUEST_CODE_STATE_TAG,
                                    f.mTargetRequestCode);
                        }
                    }

                } else {
                    fs.mSavedFragmentState = f.mSavedFragmentState;
                }

                if (DEBUG) Log.v(TAG, "Saved state of " + f + ": "
                        + fs.mSavedFragmentState);
            }
        }

        if (!haveFragments) {
            if (DEBUG) Log.v(TAG, "saveAllState: no fragments!");
            return null;
        }

        int[] added = null;
        BackStackState[] backStack = null;

        // Build list of currently added fragments.
        if (mAdded != null) {
            N = mAdded.size();
            if (N > 0) {
                added = new int[N];
                for (int i=0; i<N; i++) {
                    added[i] = mAdded.get(i).mIndex;
                    if (DEBUG) Log.v(TAG, "saveAllState: adding fragment #" + i
                            + ": " + mAdded.get(i));
                }
            }
        }

        // Now save back stack.
        if (mBackStack != null) {
            N = mBackStack.size();
            if (N > 0) {
                backStack = new BackStackState[N];
                for (int i=0; i<N; i++) {
                    backStack[i] = new BackStackState(this, mBackStack.get(i));
                    if (DEBUG) Log.v(TAG, "saveAllState: adding back stack #" + i
                            + ": " + mBackStack.get(i));
                }
            }
        }

        FragmentManagerState fms = new FragmentManagerState();
        fms.mActive = active;
        fms.mAdded = added;
        fms.mBackStack = backStack;
        return fms;
    }

    void restoreAllState(Parcelable state, ArrayList<Fragment> nonConfig) {
        // If there is no saved state at all, then there can not be
        // any nonConfig fragments either, so that is that.
        if (state == null) return;
        FragmentManagerState fms = (FragmentManagerState)state;
        if (fms.mActive == null) return;

        // First re-attach any non-config instances we are retaining back
        // to their saved state, so we don't try to instantiate them again.
        if (nonConfig != null) {
            for (int i=0; i<nonConfig.size(); i++) {
                Fragment f = nonConfig.get(i);
                if (DEBUG) Log.v(TAG, "restoreAllState: re-attaching retained " + f);
                FragmentState fs = fms.mActive[f.mIndex];
                fs.mInstance = f;
                f.mSavedViewState = null;
                f.mBackStackNesting = 0;
                f.mInLayout = false;
                f.mAdded = false;
                if (fs.mSavedFragmentState != null) {
                    fs.mSavedFragmentState.setClassLoader(mActivity.getClassLoader());
                    f.mSavedViewState = fs.mSavedFragmentState.getSparseParcelableArray(
                            FragmentManagerImpl.VIEW_STATE_TAG);
                }
            }
        }

        // Build the full list of active fragments, instantiating them from
        // their saved state.
        mActive = new ArrayList<Fragment>(fms.mActive.length);
        if (mAvailIndices != null) {
            mAvailIndices.clear();
        }
        for (int i=0; i<fms.mActive.length; i++) {
            FragmentState fs = fms.mActive[i];
            if (fs != null) {
                Fragment f = fs.instantiate(mActivity);
                if (DEBUG) Log.v(TAG, "restoreAllState: adding #" + i + ": " + f);
                mActive.add(f);
            } else {
                if (DEBUG) Log.v(TAG, "restoreAllState: adding #" + i + ": (null)");
                mActive.add(null);
                if (mAvailIndices == null) {
                    mAvailIndices = new ArrayList<Integer>();
                }
                if (DEBUG) Log.v(TAG, "restoreAllState: adding avail #" + i);
                mAvailIndices.add(i);
            }
        }

        // Update the target of all retained fragments.
        if (nonConfig != null) {
            for (int i=0; i<nonConfig.size(); i++) {
                Fragment f = nonConfig.get(i);
                if (f.mTarget != null) {
                    if (f.mTarget.mIndex < mActive.size()) {
                        f.mTarget = mActive.get(f.mTarget.mIndex);
                    } else {
                        Log.w(TAG, "Re-attaching retained fragment " + f
                                + " target no longer exists: " + f.mTarget);
                        f.mTarget = null;
                    }
                }
            }
        }

        // Build the list of currently added fragments.
        if (fms.mAdded != null) {
            mAdded = new ArrayList<Fragment>(fms.mAdded.length);
            for (int i=0; i<fms.mAdded.length; i++) {
                Fragment f = mActive.get(fms.mAdded[i]);
                if (f == null) {
                    throw new IllegalStateException(
                            "No instantiated fragment for index #" + fms.mAdded[i]);
                }
                f.mAdded = true;
                f.mImmediateActivity = mActivity;
                if (DEBUG) Log.v(TAG, "restoreAllState: making added #" + i + ": " + f);
                mAdded.add(f);
            }
        } else {
            mAdded = null;
        }

        // Build the back stack.
        if (fms.mBackStack != null) {
            mBackStack = new ArrayList<BackStackRecord>(fms.mBackStack.length);
            for (int i=0; i<fms.mBackStack.length; i++) {
                BackStackRecord bse = fms.mBackStack[i].instantiate(this);
                if (DEBUG) Log.v(TAG, "restoreAllState: adding bse #" + i
                        + " (index " + bse.mIndex + "): " + bse);
                mBackStack.add(bse);
                if (bse.mIndex >= 0) {
                    setBackStackIndex(bse.mIndex, bse);
                }
            }
        } else {
            mBackStack = null;
        }
    }

    public void attachActivity(Activity activity) {
        if (mActivity != null) throw new IllegalStateException();
        mActivity = activity;
    }

    public void noteStateNotSaved() {
        mStateSaved = false;
    }

    public void dispatchCreate() {
        mStateSaved = false;
        moveToState(Fragment.CREATED, false);
    }

    public void dispatchActivityCreated() {
        mStateSaved = false;
        moveToState(Fragment.ACTIVITY_CREATED, false);
    }

    public void dispatchStart() {
        mStateSaved = false;
        moveToState(Fragment.STARTED, false);
    }

    public void dispatchResume() {
        mStateSaved = false;
        moveToState(Fragment.RESUMED, false);
    }

    public void dispatchPause() {
        moveToState(Fragment.STARTED, false);
    }

    public void dispatchStop() {
        moveToState(Fragment.ACTIVITY_CREATED, false);
    }

    public void dispatchDestroy() {
        moveToState(Fragment.INITIALIZING, false);
        mActivity = null;
    }

    public boolean dispatchCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        boolean show = false;
        if (mActive != null) {
            for (int i=0; i<mAdded.size(); i++) {
                Fragment f = mAdded.get(i);
                if (f != null && !f.mHidden && f.mHasMenu) {
                    show = true;
                    f.onCreateOptionsMenu(menu, inflater);
                }
            }
        }
        return show;
    }

    public boolean dispatchPrepareOptionsMenu(Menu menu) {
        boolean show = false;
        if (mActive != null) {
            for (int i=0; i<mAdded.size(); i++) {
                Fragment f = mAdded.get(i);
                if (f != null && !f.mHidden && f.mHasMenu) {
                    show = true;
                    f.onPrepareOptionsMenu(menu);
                }
            }
        }
        return show;
    }

    public boolean dispatchOptionsItemSelected(MenuItem item) {
        if (mActive != null) {
            for (int i=0; i<mAdded.size(); i++) {
                Fragment f = mAdded.get(i);
                if (f != null && !f.mHidden && f.mHasMenu) {
                    if (f.onOptionsItemSelected(item)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean dispatchContextItemSelected(MenuItem item) {
        if (mActive != null) {
            for (int i=0; i<mAdded.size(); i++) {
                Fragment f = mAdded.get(i);
                if (f != null && !f.mHidden) {
                    if (f.onContextItemSelected(item)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void dispatchOptionsMenuClosed(Menu menu) {
        if (mActive != null) {
            for (int i=0; i<mAdded.size(); i++) {
                Fragment f = mAdded.get(i);
                if (f != null && !f.mHidden && f.mHasMenu) {
                    f.onOptionsMenuClosed(menu);
                }
            }
        }
    }

    public static int reverseTransit(int transit) {
        int rev = 0;
        switch (transit) {
            case FragmentTransaction.TRANSIT_FRAGMENT_OPEN:
                rev = FragmentTransaction.TRANSIT_FRAGMENT_CLOSE;
                break;
            case FragmentTransaction.TRANSIT_FRAGMENT_CLOSE:
                rev = FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
                break;
            case FragmentTransaction.TRANSIT_FRAGMENT_NEXT:
                rev = FragmentTransaction.TRANSIT_FRAGMENT_PREV;
                break;
            case FragmentTransaction.TRANSIT_FRAGMENT_PREV:
                rev = FragmentTransaction.TRANSIT_FRAGMENT_NEXT;
                break;
        }
        return rev;

    }

    public static int transitToStyleIndex(int transit, boolean enter) {
        int animAttr = -1;
        switch (transit) {
            case FragmentTransaction.TRANSIT_FRAGMENT_OPEN:
                animAttr = enter
                    ? com.android.internal.R.styleable.FragmentAnimation_fragmentOpenEnterAnimation
                    : com.android.internal.R.styleable.FragmentAnimation_fragmentOpenExitAnimation;
                break;
            case FragmentTransaction.TRANSIT_FRAGMENT_CLOSE:
                animAttr = enter
                    ? com.android.internal.R.styleable.FragmentAnimation_fragmentCloseEnterAnimation
                    : com.android.internal.R.styleable.FragmentAnimation_fragmentCloseExitAnimation;
                break;
            case FragmentTransaction.TRANSIT_FRAGMENT_NEXT:
                animAttr = enter
                    ? com.android.internal.R.styleable.FragmentAnimation_fragmentNextEnterAnimation
                    : com.android.internal.R.styleable.FragmentAnimation_fragmentNextExitAnimation;
                break;
            case FragmentTransaction.TRANSIT_FRAGMENT_PREV:
                animAttr = enter
                    ? com.android.internal.R.styleable.FragmentAnimation_fragmentPrevEnterAnimation
                    : com.android.internal.R.styleable.FragmentAnimation_fragmentPrevExitAnimation;
                break;
        }
        return animAttr;
    }
}