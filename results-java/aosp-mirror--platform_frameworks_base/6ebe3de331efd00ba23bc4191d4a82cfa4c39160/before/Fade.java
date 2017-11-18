/*
 * Copyright (C) 2013 The Android Open Source Project
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
package android.view.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * This transition tracks changes to the visibility of target views in the
 * start and end scenes and fades views in or out when they become visible
 * or non-visible. Visibility is determined by both the
 * {@link View#setVisibility(int)} state of the view as well as whether it
 * is parented in the current view hierarchy.
 */
public class Fade extends Visibility {

    private static final String LOG_TAG = "Fade";

    /**
     * Fading mode used in {@link #Fade(int)} to make the transition
     * operate on targets that are appearing. Maybe be combined with
     * {@link #OUT} to fade both in and out.
     */
    public static final int IN = 0x1;
    /**
     * Fading mode used in {@link #Fade(int)} to make the transition
     * operate on targets that are disappearing. Maybe be combined with
     * {@link #IN} to fade both in and out.
     */
    public static final int OUT = 0x2;

    private int mFadingMode;

    /**
     * Constructs a Fade transition that will fade targets in and out.
     */
    public Fade() {
        this(IN | OUT);
    }

    /**
     * Constructs a Fade transition that will fade targets in
     * and/or out, according to the value of fadingMode.
     *
     * @param fadingMode The behavior of this transition, a combination of
     * {@link #IN} and {@link #OUT}.
     */
    public Fade(int fadingMode) {
        mFadingMode = fadingMode;
    }

    /**
     * Utility method to handle creating and running the Animator.
     */
    private Animator runAnimation(View view, float startAlpha, float endAlpha,
            Animator.AnimatorListener listener) {
        final ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", startAlpha, endAlpha);
        if (listener != null) {
            anim.addListener(listener);
        }
        // TODO: Maybe extract a method into Transition to run an animation that handles the
        // duration/startDelay stuff for all subclasses.
        return anim;
    }

    @Override
    protected boolean preAppear(ViewGroup sceneRoot, View startView, int startVisibility,
            View endView, int endVisibility) {
        if ((mFadingMode & IN) != IN) {
            return false;
        }
        endView.setAlpha(0);
        return true;
    }

    @Override
    protected Animator appear(ViewGroup sceneRoot, View startView, int startVisibility,
            View endView, int endVisibility) {
        if ((mFadingMode & IN) != IN) {
            return null;
        }
        // TODO: hack - retain original value from before preAppear
        return runAnimation(endView, 0, 1, null);
        // TODO: end listener to make sure we end at 1 no matter what
    }

    @Override
    protected boolean preDisappear(ViewGroup sceneRoot, View startView, int startVisibility,
            View endView, int endVisibility) {
        if ((mFadingMode & OUT) != OUT) {
            return false;
        }
        if (Transition.DBG) {
            Log.d(LOG_TAG, "Fade.predisappear: startView, startVis, endView, endVis = " +
                        startView + ", " + startVisibility + ", " + endView + ", " + endVisibility);
        }
        View view;
        View overlayView = null;
        View viewToKeep = null;
        if (endView == null) {
            // view was removed: add the start view to the Overlay
            view = startView;
            overlayView = view;
        } else {
            // visibility change
            if (endVisibility == View.INVISIBLE) {
                view = endView;
                viewToKeep = view;
            } else {
                // Becoming GONE
                if (startView == endView) {
                    view = endView;
                    viewToKeep = view;
                } else {
                    view = startView;
                    overlayView = view;
                }
            }
        }
        // TODO: add automatic facility to Visibility superclass for keeping views around
        if (overlayView != null) {
            // TODO: Need to do this for general case of adding to overlay
            sceneRoot.getOverlay().add(overlayView);
            return true;
        }
        if (viewToKeep != null) {
            // TODO: find a different way to do this, like just changing the view to be
            // VISIBLE for the duration of the transition
            viewToKeep.setVisibility((View.VISIBLE));
            return true;
        }
        return false;
    }

    @Override
    protected Animator disappear(ViewGroup sceneRoot, View startView, int startVisibility,
            View endView, int endVisibility) {
        if ((mFadingMode & OUT) != OUT) {
            return null;
        }
        if (Transition.DBG) {
            Log.d(LOG_TAG, "Fade.disappear: startView, startVis, endView, endVis = " +
                startView + ", " + startVisibility + ", " + endView + ", " + endVisibility);
        }
        View view;
        View overlayView = null;
        View viewToKeep = null;
        final int finalVisibility = endVisibility;
        if (endView == null) {
            // view was removed: add the start view to the Overlay
            view = startView;
            overlayView = view;
        } else {
            // visibility change
            if (endVisibility == View.INVISIBLE) {
                view = endView;
                viewToKeep = view;
            } else {
                // Becoming GONE
                if (startView == endView) {
                    view = endView;
                    viewToKeep = view;
                } else {
                    view = startView;
                    overlayView = view;
                }
            }
        }
        // TODO: add automatic facility to Visibility superclass for keeping views around
        final float startAlpha = view.getAlpha();
        float endAlpha = 0;
        final View finalView = view;
        final View finalOverlayView = overlayView;
        final View finalViewToKeep = viewToKeep;
        final ViewGroup finalSceneRoot = sceneRoot;
        final Animator.AnimatorListener endListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finalView.setAlpha(startAlpha);
                // TODO: restore view offset from overlay repositioning
                if (finalViewToKeep != null) {
                    finalViewToKeep.setVisibility(finalVisibility);
                }
                if (finalOverlayView != null) {
                    finalSceneRoot.getOverlay().remove(finalOverlayView);
                }
            }
        };
        return runAnimation(view, startAlpha, endAlpha, endListener);
    }

}