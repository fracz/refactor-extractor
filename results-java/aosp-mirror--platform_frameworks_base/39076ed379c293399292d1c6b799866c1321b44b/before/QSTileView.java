/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile.State;

/** View that represents a standard quick settings tile. **/
public class QSTileView extends ViewGroup {
    private static final Typeface CONDENSED = Typeface.create("sans-serif-condensed",
            Typeface.NORMAL);

    protected final Context mContext;
    private final View mIcon;
    private final View mDivider;
    private final H mHandler = new H();
    private final int mIconSizePx;
    private final int mTileSpacingPx;
    private final int mTilePaddingTopPx;
    private final int mTilePaddingBelowIconPx;
    private final int mDualTilePaddingBelowDividerPx;

    private TextView mLabel;
    private QSDualTileLabel mDualLabel;
    private boolean mDual;
    private OnClickListener mClickPrimary;
    private OnClickListener mClickSecondary;
    private RippleDrawable mRipple;

    public QSTileView(Context context) {
        super(context);

        mContext = context;
        final Resources res = context.getResources();
        mIconSizePx = res.getDimensionPixelSize(R.dimen.qs_tile_icon_size);
        mTileSpacingPx = res.getDimensionPixelSize(R.dimen.qs_tile_spacing);
        mTilePaddingTopPx = res.getDimensionPixelSize(R.dimen.qs_tile_padding_top);
        mTilePaddingBelowIconPx =  res.getDimensionPixelSize(R.dimen.qs_tile_padding_below_icon);
        mDualTilePaddingBelowDividerPx =
                res.getDimensionPixelSize(R.dimen.qs_dual_tile_padding_below_divider);
        recreateLabel();
        setClipChildren(false);

        mIcon = createIcon();
        addView(mIcon);

        mDivider = new View(mContext);
        mDivider.setBackgroundColor(res.getColor(R.color.qs_tile_divider));
        final int dh = res.getDimensionPixelSize(R.dimen.qs_tile_divider_height);
        mDivider.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dh));
        addView(mDivider);

        setClickable(true);
        setBackground(getTileBackground());
    }

    private void recreateLabel() {
        CharSequence labelText = null;
        if (mLabel != null) {
            labelText = mLabel.getText();
            removeView(mLabel);
            mLabel = null;
        }
        if (mDualLabel != null) {
            labelText = mDualLabel.getText();
            removeView(mDualLabel);
            mDualLabel = null;
        }
        final Resources res = mContext.getResources();
        if (mDual) {
            final Context c = new ContextThemeWrapper(mContext, R.style.BorderlessButton_Tiny);
            mDualLabel = new QSDualTileLabel(c);
            mDualLabel.setId(android.R.id.title);
            mDualLabel.setFirstLineBackground(res.getDrawable(R.drawable.qs_dual_tile_caret));
            mDualLabel.setTextColor(res.getColor(R.color.qs_tile_text));
            mDualLabel.setPadding(0, mDualTilePaddingBelowDividerPx, 0, 0);
            mDualLabel.setTypeface(CONDENSED);
            mDualLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    res.getDimensionPixelSize(R.dimen.qs_tile_text_size));
            mDualLabel.setClickable(true);
            mDualLabel.setOnClickListener(mClickSecondary);
            if (labelText != null) {
                mDualLabel.setText(labelText);
            }
            addView(mDualLabel);
        } else {
            mLabel = new TextView(mContext);
            mLabel.setId(android.R.id.title);
            mLabel.setTextColor(res.getColor(R.color.qs_tile_text));
            mLabel.setGravity(Gravity.CENTER_HORIZONTAL);
            mLabel.setMinLines(2);
            mLabel.setPadding(0, 0, 0, 0);
            mLabel.setTypeface(CONDENSED);
            mLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    res.getDimensionPixelSize(R.dimen.qs_tile_text_size));
            mLabel.setClickable(false);
            if (labelText != null) {
                mLabel.setText(labelText);
            }
            addView(mLabel);
        }
    }

    public void setDual(boolean dual) {
        final boolean changed = dual != mDual;
        mDual = dual;
        if (changed) {
            recreateLabel();
        }
        setOnClickListener(mClickPrimary);
        mDivider.setVisibility(dual ? VISIBLE : GONE);
        postInvalidate();
    }

    public void init(OnClickListener clickPrimary, OnClickListener clickSecondary) {
        mClickPrimary = clickPrimary;
        mClickSecondary = clickSecondary;
    }

    protected View createIcon() {
        final ImageView icon = new ImageView(mContext);
        icon.setId(android.R.id.icon);
        icon.setScaleType(ScaleType.CENTER_INSIDE);
        return icon;
    }

    private Drawable getTileBackground() {
        final int[] attrs = new int[] { android.R.attr.selectableItemBackgroundBorderless };
        final TypedArray ta = mContext.obtainStyledAttributes(attrs);
        final Drawable d = ta.getDrawable(0);
        ta.recycle();
        if (d instanceof RippleDrawable) {
            mRipple = (RippleDrawable) d;
        }
        return d;
    }

    private View labelView() {
        return mDual ? mDualLabel : mLabel;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int w = MeasureSpec.getSize(widthMeasureSpec);
        final int h = MeasureSpec.getSize(heightMeasureSpec);
        final int iconSpec = exactly(mIconSizePx);
        mIcon.measure(iconSpec, iconSpec);
        labelView().measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
        if (mDual) {
            mDivider.measure(widthMeasureSpec, exactly(mDivider.getLayoutParams().height));
        }
        setMeasuredDimension(w, h);
    }

    private static int exactly(int size) {
        return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int w = getMeasuredWidth();

        int top = 0;
        top += mTileSpacingPx;
        top += mTilePaddingTopPx;
        final int iconLeft = (w - mIcon.getMeasuredWidth()) / 2;
        layout(mIcon, iconLeft, top);
        if (mRipple != null) {
            // center the touch feedback on the center of the icon, and dial it down a bit
            final int cx = w / 2;
            final int cy = mIcon.getTop() + mIcon.getHeight() / 2;
            final int rad = (int)(mIcon.getHeight() * 1.5);
            mRipple.setHotspotBounds(cx - rad, cy - rad, cx + rad, cy + rad);
        }
        top = mIcon.getBottom();
        top += mTilePaddingBelowIconPx;
        if (mDual) {
            layout(mDivider, 0, top);
            top = mDivider.getBottom();
        }
        layout(labelView(), 0, top);
    }

    private static void layout(View child, int left, int top) {
        child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
    }

    protected void handleStateChanged(QSTile.State state) {
        if (mIcon instanceof ImageView) {
            ImageView iv = (ImageView) mIcon;
            if (state.icon != null) {
                iv.setImageDrawable(state.icon);
            } else if (state.iconId > 0) {
                iv.setImageResource(state.iconId);
            }
        }
        if (mDual) {
            mDualLabel.setText(state.label);
        } else {
            mLabel.setText(state.label);
        }
        setContentDescription(state.contentDescription);
    }

    public void onStateChanged(QSTile.State state) {
        mHandler.obtainMessage(H.STATE_CHANGED, state).sendToTarget();
    }

    private class H extends Handler {
        private static final int STATE_CHANGED = 1;
        public H() {
            super(Looper.getMainLooper());
        }
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == STATE_CHANGED) {
                handleStateChanged((State) msg.obj);
            }
        }
    }
}