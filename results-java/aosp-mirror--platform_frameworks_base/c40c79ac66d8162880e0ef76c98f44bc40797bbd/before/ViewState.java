/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui.statusbar.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.Interpolator;

import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.policy.HeadsUpManager;

/**
 * A state of a view. This can be used to apply a set of view properties to a view with
 * {@link com.android.systemui.statusbar.stack.StackScrollState} or start animations with
 * {@link com.android.systemui.statusbar.stack.StackStateAnimator}.
*/
public class ViewState {

    private static final int TAG_ANIMATOR_TRANSLATION_Y = R.id.translation_y_animator_tag;
    private static final int TAG_ANIMATOR_TRANSLATION_Z = R.id.translation_z_animator_tag;
    private static final int TAG_ANIMATOR_ALPHA = R.id.alpha_animator_tag;
    private static final int TAG_END_TRANSLATION_Y = R.id.translation_y_animator_end_value_tag;
    private static final int TAG_END_TRANSLATION_Z = R.id.translation_z_animator_end_value_tag;
    private static final int TAG_END_ALPHA = R.id.alpha_animator_end_value_tag;
    private static final int TAG_START_TRANSLATION_Y = R.id.translation_y_animator_start_value_tag;
    private static final int TAG_START_TRANSLATION_Z = R.id.translation_z_animator_start_value_tag;
    private static final int TAG_START_ALPHA = R.id.alpha_animator_start_value_tag;

    public float alpha;
    public float xTranslation;
    public float yTranslation;
    public float zTranslation;
    public boolean gone;
    public boolean hidden;
    public float scaleX = 1.0f;
    public float scaleY = 1.0f;

    public void copyFrom(ViewState viewState) {
        alpha = viewState.alpha;
        xTranslation = viewState.xTranslation;
        yTranslation = viewState.yTranslation;
        zTranslation = viewState.zTranslation;
        gone = viewState.gone;
        hidden = viewState.hidden;
        scaleX = viewState.scaleX;
        scaleY = viewState.scaleY;
    }

    public void initFrom(View view) {
        alpha = view.getAlpha();
        xTranslation = view.getTranslationX();
        yTranslation = view.getTranslationY();
        zTranslation = view.getTranslationZ();
        gone = view.getVisibility() == View.GONE;
        hidden = false;
        scaleX = view.getScaleX();
        scaleY = view.getScaleY();
    }

    /**
     * Applies a {@link ViewState} to a normal view.
     */
    public void applyToView(View view) {
        if (this.gone) {
            // don't do anything with it
            return;
        }
        boolean becomesInvisible = this.alpha == 0.0f || this.hidden;
        if (view.getAlpha() != this.alpha) {
            // apply layer type
            boolean becomesFullyVisible = this.alpha == 1.0f;
            boolean newLayerTypeIsHardware = !becomesInvisible && !becomesFullyVisible
                    && view.hasOverlappingRendering();
            int layerType = view.getLayerType();
            int newLayerType = newLayerTypeIsHardware
                    ? View.LAYER_TYPE_HARDWARE
                    : View.LAYER_TYPE_NONE;
            if (layerType != newLayerType) {
                view.setLayerType(newLayerType, null);
            }

            // apply alpha
            view.setAlpha(this.alpha);
        }

        // apply visibility
        int oldVisibility = view.getVisibility();
        int newVisibility = becomesInvisible ? View.INVISIBLE : View.VISIBLE;
        if (newVisibility != oldVisibility) {
            if (!(view instanceof ExpandableView) || !((ExpandableView) view).willBeGone()) {
                // We don't want views to change visibility when they are animating to GONE
                view.setVisibility(newVisibility);
            }
        }

        // apply xTranslation
        if (view.getTranslationX() != this.xTranslation) {
            view.setTranslationX(this.xTranslation);
        }

        // apply yTranslation
        if (view.getTranslationY() != this.yTranslation) {
            view.setTranslationY(this.yTranslation);
        }

        // apply zTranslation
        if (view.getTranslationZ() != this.zTranslation) {
            view.setTranslationZ(this.zTranslation);
        }

        // apply scaleX
        if (view.getScaleX() != this.scaleX) {
            view.setScaleX(this.scaleX);
        }

        // apply scaleY
        if (view.getScaleY() != this.scaleY) {
            view.setScaleY(this.scaleY);
        }
    }

    /**
     * Start an animation to this viewstate
     * @param child the view to animate
     * @param animationProperties the properties of the animation
     */
    public void animateTo(View child, AnimationProperties animationProperties) {
        boolean wasVisible = child.getVisibility() == View.VISIBLE;
        final float alpha = this.alpha;
        if (!wasVisible && (alpha != 0 || child.getAlpha() != 0)
                && !this.gone && !this.hidden) {
            child.setVisibility(View.VISIBLE);
        }
        boolean yTranslationChanging = child.getTranslationY() != this.yTranslation;
        boolean zTranslationChanging = child.getTranslationZ() != this.zTranslation;
        float childAlpha = child.getAlpha();
        boolean alphaChanging = this.alpha != childAlpha;
        if (child instanceof ExpandableView) {
            // We don't want views to change visibility when they are animating to GONE
            alphaChanging &= !((ExpandableView) child).willBeGone();
        }

        // start translationY animation
        if (yTranslationChanging) {
            startYTranslationAnimation(child, animationProperties);
        } else {
            abortAnimation(child, TAG_ANIMATOR_TRANSLATION_Y);
        }

        // start translationZ animation
        if (zTranslationChanging) {
            startZTranslationAnimation(child, animationProperties);
        } else {
            abortAnimation(child, TAG_ANIMATOR_TRANSLATION_Z);
        }

        // start alpha animation
        if (alphaChanging && child.getTranslationX() == 0) {
            startAlphaAnimation(child, animationProperties);
        }  else {
            abortAnimation(child, TAG_ANIMATOR_ALPHA);
        }
    }

    private void startAlphaAnimation(final View child, AnimationProperties properties) {
        Float previousStartValue = getChildTag(child,TAG_START_ALPHA);
        Float previousEndValue = getChildTag(child,TAG_END_ALPHA);
        final float newEndValue = this.alpha;
        if (previousEndValue != null && previousEndValue == newEndValue) {
            return;
        }
        ObjectAnimator previousAnimator = getChildTag(child, TAG_ANIMATOR_ALPHA);
        AnimationFilter filter = properties.getAnimationFilter();
        if (!filter.animateAlpha) {
            // just a local update was performed
            if (previousAnimator != null) {
                // we need to increase all animation keyframes of the previous animator by the
                // relative change to the end value
                PropertyValuesHolder[] values = previousAnimator.getValues();
                float relativeDiff = newEndValue - previousEndValue;
                float newStartValue = previousStartValue + relativeDiff;
                values[0].setFloatValues(newStartValue, newEndValue);
                child.setTag(TAG_START_ALPHA, newStartValue);
                child.setTag(TAG_END_ALPHA, newEndValue);
                previousAnimator.setCurrentPlayTime(previousAnimator.getCurrentPlayTime());
                return;
            } else {
                // no new animation needed, let's just apply the value
                child.setAlpha(newEndValue);
                if (newEndValue == 0) {
                    child.setVisibility(View.INVISIBLE);
                }
            }
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(child, View.ALPHA,
                child.getAlpha(), newEndValue);
        animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        // Handle layer type
        child.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        animator.addListener(new AnimatorListenerAdapter() {
            public boolean mWasCancelled;

            @Override
            public void onAnimationEnd(Animator animation) {
                child.setLayerType(View.LAYER_TYPE_NONE, null);
                if (newEndValue == 0 && !mWasCancelled) {
                    child.setVisibility(View.INVISIBLE);
                }
                // remove the tag when the animation is finished
                child.setTag(TAG_ANIMATOR_ALPHA, null);
                child.setTag(TAG_START_ALPHA, null);
                child.setTag(TAG_END_ALPHA, null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mWasCancelled = true;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mWasCancelled = false;
            }
        });
        long newDuration = cancelAnimatorAndGetNewDuration(properties.duration, previousAnimator);
        animator.setDuration(newDuration);
        if (properties.delay > 0 && (previousAnimator == null
                || previousAnimator.getAnimatedFraction() == 0)) {
            animator.setStartDelay(properties.delay);
        }
        AnimatorListenerAdapter listener = properties.getAnimationFinishListener();
        if (listener != null) {
            animator.addListener(listener);
        }

        startAnimator(animator, listener);
        child.setTag(TAG_ANIMATOR_ALPHA, animator);
        child.setTag(TAG_START_ALPHA, child.getAlpha());
        child.setTag(TAG_END_ALPHA, newEndValue);
    }

    private void startZTranslationAnimation(final View child, AnimationProperties properties) {
        Float previousStartValue = getChildTag(child,TAG_START_TRANSLATION_Z);
        Float previousEndValue = getChildTag(child,TAG_END_TRANSLATION_Z);
        float newEndValue = this.zTranslation;
        if (previousEndValue != null && previousEndValue == newEndValue) {
            return;
        }
        ObjectAnimator previousAnimator = getChildTag(child, TAG_ANIMATOR_TRANSLATION_Z);
        AnimationFilter filter = properties.getAnimationFilter();
        if (!filter.animateZ) {
            // just a local update was performed
            if (previousAnimator != null) {
                // we need to increase all animation keyframes of the previous animator by the
                // relative change to the end value
                PropertyValuesHolder[] values = previousAnimator.getValues();
                float relativeDiff = newEndValue - previousEndValue;
                float newStartValue = previousStartValue + relativeDiff;
                values[0].setFloatValues(newStartValue, newEndValue);
                child.setTag(TAG_START_TRANSLATION_Z, newStartValue);
                child.setTag(TAG_END_TRANSLATION_Z, newEndValue);
                previousAnimator.setCurrentPlayTime(previousAnimator.getCurrentPlayTime());
                return;
            } else {
                // no new animation needed, let's just apply the value
                child.setTranslationZ(newEndValue);
            }
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Z,
                child.getTranslationZ(), newEndValue);
        animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        long newDuration = cancelAnimatorAndGetNewDuration(properties.duration, previousAnimator);
        animator.setDuration(newDuration);
        if (properties.delay > 0 && (previousAnimator == null
                || previousAnimator.getAnimatedFraction() == 0)) {
            animator.setStartDelay(properties.delay);
        }
        AnimatorListenerAdapter listener = properties.getAnimationFinishListener();
        if (listener != null) {
            animator.addListener(listener);
        }
        // remove the tag when the animation is finished
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                child.setTag(TAG_ANIMATOR_TRANSLATION_Z, null);
                child.setTag(TAG_START_TRANSLATION_Z, null);
                child.setTag(TAG_END_TRANSLATION_Z, null);
            }
        });
        startAnimator(animator, listener);
        child.setTag(TAG_ANIMATOR_TRANSLATION_Z, animator);
        child.setTag(TAG_START_TRANSLATION_Z, child.getTranslationZ());
        child.setTag(TAG_END_TRANSLATION_Z, newEndValue);
    }

    private void startYTranslationAnimation(final View child, AnimationProperties properties) {
        Float previousStartValue = getChildTag(child,TAG_START_TRANSLATION_Y);
        Float previousEndValue = getChildTag(child,TAG_END_TRANSLATION_Y);
        float newEndValue = this.yTranslation;
        if (previousEndValue != null && previousEndValue == newEndValue) {
            return;
        }
        ObjectAnimator previousAnimator = getChildTag(child, TAG_ANIMATOR_TRANSLATION_Y);
        AnimationFilter filter = properties.getAnimationFilter();
        if (!filter.animateY) {
            // just a local update was performed
            if (previousAnimator != null) {
                // we need to increase all animation keyframes of the previous animator by the
                // relative change to the end value
                PropertyValuesHolder[] values = previousAnimator.getValues();
                float relativeDiff = newEndValue - previousEndValue;
                float newStartValue = previousStartValue + relativeDiff;
                values[0].setFloatValues(newStartValue, newEndValue);
                child.setTag(TAG_START_TRANSLATION_Y, newStartValue);
                child.setTag(TAG_END_TRANSLATION_Y, newEndValue);
                previousAnimator.setCurrentPlayTime(previousAnimator.getCurrentPlayTime());
                return;
            } else {
                // no new animation needed, let's just apply the value
                child.setTranslationY(newEndValue);
                return;
            }
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y,
                child.getTranslationY(), newEndValue);
        Interpolator customInterpolator = properties.getCustomInterpolator(child,
                View.TRANSLATION_Y);
        Interpolator interpolator =  customInterpolator != null ? customInterpolator
                : Interpolators.FAST_OUT_SLOW_IN;
        animator.setInterpolator(interpolator);
        long newDuration = cancelAnimatorAndGetNewDuration(properties.duration, previousAnimator);
        animator.setDuration(newDuration);
        if (properties.delay > 0 && (previousAnimator == null
                || previousAnimator.getAnimatedFraction() == 0)) {
            animator.setStartDelay(properties.delay);
        }
        AnimatorListenerAdapter listener = properties.getAnimationFinishListener();
        if (listener != null) {
            animator.addListener(listener);
        }
        // remove the tag when the animation is finished
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                HeadsUpManager.setIsClickedNotification(child, false);
                child.setTag(TAG_ANIMATOR_TRANSLATION_Y, null);
                child.setTag(TAG_START_TRANSLATION_Y, null);
                child.setTag(TAG_END_TRANSLATION_Y, null);
                onYTranslationAnimationFinished();
            }
        });
        startAnimator(animator, listener);
        child.setTag(TAG_ANIMATOR_TRANSLATION_Y, animator);
        child.setTag(TAG_START_TRANSLATION_Y, child.getTranslationY());
        child.setTag(TAG_END_TRANSLATION_Y, newEndValue);
    }

    protected void onYTranslationAnimationFinished() {
    }

    protected void startAnimator(Animator animator, AnimatorListenerAdapter listener) {
        if (listener != null) {
            // Even if there's a delay we'd want to notify it of the start immediately.
            listener.onAnimationStart(animator);
        }
        animator.start();
    }

    public static <T> T getChildTag(View child, int tag) {
        return (T) child.getTag(tag);
    }

    protected void abortAnimation(View child, int animatorTag) {
        Animator previousAnimator = getChildTag(child, animatorTag);
        if (previousAnimator != null) {
            previousAnimator.cancel();
        }
    }

    /**
     * Cancel the previous animator and get the duration of the new animation.
     *
     * @param duration the new duration
     * @param previousAnimator the animator which was running before
     * @return the new duration
     */
    protected long cancelAnimatorAndGetNewDuration(long duration, ValueAnimator previousAnimator) {
        long newDuration = duration;
        if (previousAnimator != null) {
            // We take either the desired length of the new animation or the remaining time of
            // the previous animator, whichever is longer.
            newDuration = Math.max(previousAnimator.getDuration()
                    - previousAnimator.getCurrentPlayTime(), newDuration);
            previousAnimator.cancel();
        }
        return newDuration;
    }

    /**
     * Get the end value of the yTranslation animation running on a view or the yTranslation
     * if no animation is running.
     */
    public static float getFinalTranslationY(View view) {
        if (view == null) {
            return 0;
        }
        ValueAnimator yAnimator = getChildTag(view, TAG_ANIMATOR_TRANSLATION_Y);
        if (yAnimator == null) {
            return view.getTranslationY();
        } else {
            return getChildTag(view, TAG_END_TRANSLATION_Y);
        }
    }

    public static boolean isAnimatingY(ExpandableView child) {
        return getChildTag(child, TAG_ANIMATOR_TRANSLATION_Y) != null;
    }
}