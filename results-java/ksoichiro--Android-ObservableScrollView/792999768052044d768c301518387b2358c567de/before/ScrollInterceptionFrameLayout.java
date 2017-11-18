/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ksoichiro.android.observablescrollview.samples;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class ScrollInterceptionFrameLayout extends FrameLayout {

    public interface ScrollInterceptionListener {
        boolean shouldInterceptTouchEvent(MotionEvent ev, boolean moving, float diffY);

        void onDownMotionEvent(MotionEvent ev);

        void onMoveMotionEvent(MotionEvent ev, float y, float diffY);

        void onUpOrCancelMotionEvent(MotionEvent ev);
    }

    private boolean mIntercepting;
    private boolean mDownMotionEventPended;
    private boolean mBeganFromDownMotionEvent;
    private float mInitialY;
    private MotionEvent mPendingDownMotionEvent;
    private ScrollInterceptionListener mScrollInterceptionListener;

    public ScrollInterceptionFrameLayout(Context context) {
        super(context);
    }

    public ScrollInterceptionFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollInterceptionFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScrollInterceptionFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setScrollInterceptionListener(ScrollInterceptionListener listener) {
        mScrollInterceptionListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mScrollInterceptionListener == null) {
            return super.onInterceptTouchEvent(ev);
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mInitialY = ev.getY();
                mPendingDownMotionEvent = MotionEvent.obtainNoHistory(ev);
                mBeganFromDownMotionEvent = true;
                mDownMotionEventPended = true;
                mIntercepting = mScrollInterceptionListener.shouldInterceptTouchEvent(ev, false, 0);
                return mIntercepting;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mScrollInterceptionListener != null) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mScrollInterceptionListener.onDownMotionEvent(ev);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float diffY = ev.getY() - mInitialY;
                    mIntercepting = mScrollInterceptionListener.shouldInterceptTouchEvent(ev, true, diffY);
                    if (mIntercepting) {
                        if (!mBeganFromDownMotionEvent) {
                            mBeganFromDownMotionEvent = true;

                            // Layout didn't receive ACTION_DOWN motion event,
                            // so generate down motion event with current position.
                            MotionEvent event = MotionEvent.obtainNoHistory(mPendingDownMotionEvent);
                            event.setLocation(ev.getX(), ev.getY());
                            mInitialY = ev.getY();
                            diffY = 0;
                            mScrollInterceptionListener.onDownMotionEvent(event);
                        }
                        mScrollInterceptionListener.onMoveMotionEvent(ev, ev.getY(), diffY);

                        // If next mIntercepting become false,
                        // then we should generate fake ACTION_DOWN event.
                        // Therefore we set pending flag to true as if this is a down motion event.
                        mDownMotionEventPended = true;
                    } else {
                        final boolean downMotionEventPended = mDownMotionEventPended;
                        if (mDownMotionEventPended) {
                            mDownMotionEventPended = false;
                        }

                        // We want to dispatch a down motion event and this ev event to
                        // child views, but calling dispatchTouchEvent() causes StackOverflowError.
                        // Therefore we do it manually.
                        for (int i = getChildCount() - 1; 0 <= i; i--) {
                            View childView = getChildAt(i);
                            if (childView != null) {
                                Rect childRect = new Rect();
                                childView.getHitRect(childRect);
                                if (!childRect.contains((int) ev.getX(), (int) ev.getY())) {
                                    continue;
                                }
                                boolean consumed = false;
                                if (downMotionEventPended) {
                                    // Update location to prevent the point jumping
                                    consumed = childView.onTouchEvent(mPendingDownMotionEvent);
                                }
                                consumed |= childView.onTouchEvent(ev);
                                if (consumed) {
                                    break;
                                }
                            }
                        }

                        // If next mIntercepting become true,
                        // then we should generate fake ACTION_DOWN event.
                        // Therefore we set beganFromDownMotionEvent flag to false
                        // as if we haven't received a down motion event.
                        mBeganFromDownMotionEvent = false;
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mBeganFromDownMotionEvent = false;
                    if (mIntercepting) {
                        mScrollInterceptionListener.onUpOrCancelMotionEvent(ev);
                    } else {
                        for (int i = 0; i < getChildCount(); i++) {
                            View childView = getChildAt(i);
                            if (childView != null) {
                                if (mDownMotionEventPended) {
                                    mDownMotionEventPended = false;
                                    childView.onTouchEvent(mPendingDownMotionEvent);
                                }
                                childView.onTouchEvent(ev);
                            }
                        }
                    }
                    return true;
            }
        }
        return super.onTouchEvent(ev);
    }
}