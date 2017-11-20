/*
 * Copyright (c) 2016 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.navigation.ui;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.transitionseverywhere.ChangeTransform;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import me.zhanghai.android.douya.R;
import me.zhanghai.android.douya.account.util.AccountUtils;
import me.zhanghai.android.douya.network.api.info.apiv2.User;
import me.zhanghai.android.douya.network.api.info.apiv2.UserInfo;
import me.zhanghai.android.douya.ui.CrossfadeText;
import me.zhanghai.android.douya.util.DrawableUtils;
import me.zhanghai.android.douya.util.ImageUtils;
import me.zhanghai.android.douya.util.ViewCompat;
import me.zhanghai.android.douya.util.ViewUtils;

public class NavigationHeaderLayout extends FrameLayout {

    @BindView(R.id.backdrop)
    ImageView mBackdropImage;
    @BindView(R.id.scrim)
    View mScrimView;
    @BindView(R.id.avatar)
    ImageView mAvatarImage;
    @BindView(R.id.fade_out_avatar)
    ImageView mFadeOutAvatarImage;
    @BindView(R.id.recent_one_avatar)
    ImageView mRecentOneAvatarImage;
    @BindView(R.id.fade_out_recent_one_avatar)
    ImageView mFadeOutRecentOneAvatarImage;
    @BindView(R.id.recent_two_avatar)
    ImageView mRecentTwoAvatarImage;
    @BindView(R.id.fade_out_recent_two_avatar)
    ImageView mFadeOutRecentTwoAvatarImage;
    @BindViews({R.id.avatar, R.id.fade_out_avatar,
            R.id.recent_one_avatar, R.id.fade_out_recent_one_avatar,
            R.id.recent_two_avatar, R.id.fade_out_recent_two_avatar})
    ImageView[] mAvatarImages;
    @BindView(R.id.info)
    LinearLayout mInfoLayout;
    @BindView(R.id.name)
    TextView mNameText;
    @BindView(R.id.description)
    TextView mDescriptionText;
    @BindView(R.id.dropDown)
    ImageView mDropDownImage;

    private Adapter mAdapter;
    private Listener mListener;

    private Account mActiveAccount;
    private Account mRecentOneAccount;
    private Account mRecentTwoAccount;

    private boolean mTransitionRunning;
    private boolean mShowingAccountList;

    public NavigationHeaderLayout(Context context) {
        super(context);

        init();
    }

    public NavigationHeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public NavigationHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NavigationHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                  int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.navigation_header_layout, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this);

        ViewCompat.setBackground(mScrimView, DrawableUtils.makeScrimDrawable());
        mInfoLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleShowingAccountList();
            }
        });
    }

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void bind() {

        if (mAdapter == null) {
            return;
        }

        bindActiveUser();
        bindRecentUsers();
    }

    private void bindActiveUser() {

        Context context = getContext();
        mActiveAccount = AccountUtils.getActiveAccount(context);

        UserInfo userInfo = mAdapter.getUserInfo(mActiveAccount);
        if (userInfo != null) {
            bindAvatarImage(mAvatarImage, userInfo.getLargeAvatarOrAvatar());
            mNameText.setText(userInfo.name);
            mDescriptionText.setText(userInfo.signature);
        } else {
            User partialUser = mAdapter.getPartialUser(mActiveAccount);
            bindAvatarImage(mAvatarImage, null);
            mNameText.setText(partialUser.name);
            //noinspection deprecation
            mDescriptionText.setText(partialUser.uid);
        }
        mAvatarImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.openProfile(mActiveAccount);
                }
            }
        });
        mBackdropImage.setImageResource(R.color.grey_200);
    }

    private void bindRecentUsers() {
        Context context = getContext();
        mRecentOneAccount = AccountUtils.getRecentOneAccount(context);
        bindRecentUser(mRecentOneAvatarImage, mRecentOneAccount);
        mRecentTwoAccount = AccountUtils.getRecentTwoAccount(context);
        bindRecentUser(mRecentTwoAvatarImage, mRecentTwoAccount);
    }

    private void bindRecentUser(ImageView avatarImage, final Account account) {

        if (account == null) {
            avatarImage.setVisibility(GONE);
            return;
        }

        UserInfo userInfo = mAdapter.getUserInfo(account);
        if (userInfo != null) {
            bindAvatarImage(avatarImage, userInfo.getLargeAvatarOrAvatar());
        } else {
            bindAvatarImage(avatarImage, null);
        }
        avatarImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTransitionRunning) {
                    return;
                }
                switchToAccount(account);
            }
        });
    }

    private void bindAvatarImage(ImageView avatarImage, String avatarUrl) {

        if (TextUtils.isEmpty(avatarUrl)) {
            avatarImage.setImageResource(R.drawable.avatar_icon_white_inactive_64dp);
            avatarImage.setTag(null);
            return;
        }

        for (ImageView anotherAvatarImage : mAvatarImages) {
            String anotherAvatarUrl = (String) anotherAvatarImage.getTag();
            if (TextUtils.equals(anotherAvatarUrl , avatarUrl)) {
                setAvatarImageFrom(avatarImage, anotherAvatarImage);
                return;
            }
        }

        ImageUtils.loadNavigationAvatar(avatarImage, avatarUrl, getContext());
    }

    private void setAvatarImageFrom(ImageView toAvatarImage, ImageView fromAvatarImage) {
        if (toAvatarImage == fromAvatarImage) {
            return;
        }
        toAvatarImage.setImageDrawable(fromAvatarImage.getDrawable());
        toAvatarImage.setTag(fromAvatarImage.getTag());
    }

    public void switchToAccount(Account account) {

        Context context = getContext();
        if (AccountUtils.isActiveAccount(account, context)) {
            return;
        }

        AccountUtils.setActiveAccount(account, context);
        if (account.equals(mRecentOneAccount)) {
            beginAvatarTransitionFromRecent(mRecentOneAvatarImage);
        } else if (account.equals(mRecentTwoAccount)) {
            beginAvatarTransitionFromRecent(mRecentTwoAvatarImage);
        } else {
            beginAvatarTransitionFromNonRecent();
        }
        bind();

        // TODO: Move to ActiveAccountChangedEvent.
        if (mListener != null) {
            mListener.onActiveAccountChanged(account);
        }
    }

    private void beginAvatarTransitionFromRecent(ImageView recentAvatarImage) {
        beginAccountTransition(recentAvatarImage, mAvatarImage, null);
    }

    private void beginAvatarTransitionFromNonRecent() {
        boolean hasRecentTwoAccount = mRecentTwoAccount != null;
        beginAccountTransition(mAvatarImage, mRecentOneAvatarImage,
                hasRecentTwoAccount ? mRecentTwoAvatarImage : null);
    }

    private void beginAccountTransition(ImageView moveAvatarOneImage,
                                        ImageView moveAvatarTwoImage,
                                        ImageView moveAvatarThreeImage) {

        ImageView appearAvatarImage = moveAvatarOneImage;
        ImageView disappearAvatarImage = moveAvatarThreeImage != null ? moveAvatarThreeImage
                : moveAvatarTwoImage;
        ImageView fadeOutDisappearAvatarImage =
                disappearAvatarImage == mAvatarImage ? mFadeOutAvatarImage
                        : disappearAvatarImage == mRecentOneAvatarImage ?
                        mFadeOutRecentOneAvatarImage : mFadeOutRecentTwoAvatarImage;

        TransitionSet transitionSet = new TransitionSet();
        int duration = ViewUtils.getLongAnimTime(getContext());
        // Will be set on already added and newly added transitions.
        transitionSet.setDuration(duration);
        // NOTE: TransitionSet.setInterpolator() won't have any effect on platform versions.
        // https://code.google.com/p/android/issues/detail?id=195495
        transitionSet.setInterpolator(new FastOutSlowInInterpolator());

        Fade fadeOutAvatar = new Fade(Fade.OUT);
        setAvatarImageFrom(fadeOutDisappearAvatarImage, disappearAvatarImage);
        fadeOutDisappearAvatarImage.setVisibility(VISIBLE);
        fadeOutAvatar.addTarget(fadeOutDisappearAvatarImage);
        transitionSet.addTransition(fadeOutAvatar);
        // Make it finish before new avatar arrives.
        fadeOutAvatar.setDuration(duration / 2);

        Fade fadeInAvatar = new Fade(Fade.IN);
        appearAvatarImage.setVisibility(INVISIBLE);
        fadeInAvatar.addTarget(appearAvatarImage);
        transitionSet.addTransition(fadeInAvatar);

        ChangeTransform changeAppearAvatarTransform = new ChangeTransform();
        appearAvatarImage.setScaleX(0.8f);
        appearAvatarImage.setScaleY(0.8f);
        changeAppearAvatarTransform.addTarget(appearAvatarImage);
        transitionSet.addTransition(changeAppearAvatarTransform);

        addChangeMoveToAvatarTransformToTransitionSet(moveAvatarOneImage, moveAvatarTwoImage,
                transitionSet);

        if (moveAvatarThreeImage != null) {
            addChangeMoveToAvatarTransformToTransitionSet(moveAvatarTwoImage, moveAvatarThreeImage,
                    transitionSet);
        }

        CrossfadeText crossfadeText = new CrossfadeText();
        crossfadeText.addTarget(mNameText);
        crossfadeText.addTarget(mDescriptionText);
        transitionSet.addTransition(crossfadeText);

        transitionSet.addListener(new Transition.TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                mTransitionRunning = false;
            }
        });
        TransitionManager.beginDelayedTransition(this, transitionSet);
        mTransitionRunning = true;

        fadeOutDisappearAvatarImage.setVisibility(INVISIBLE);

        appearAvatarImage.setVisibility(VISIBLE);
        appearAvatarImage.setScaleX(1);
        appearAvatarImage.setScaleY(1);

        resetMoveToAvatarTransform(moveAvatarTwoImage);
        if (moveAvatarThreeImage != null) {
            resetMoveToAvatarTransform(moveAvatarThreeImage);
        }
    }

    private void addChangeMoveToAvatarTransformToTransitionSet(ImageView moveFromAvatarImage,
                                                               ImageView moveToAvatarImage,
                                                               TransitionSet transitionSet) {
        ChangeTransform changeMoveToAvatarTransform = new ChangeTransform();
        moveToAvatarImage.setX(moveFromAvatarImage.getLeft()
                + (moveFromAvatarImage.getWidth() - moveToAvatarImage.getWidth()) / 2);
        moveToAvatarImage.setY(moveFromAvatarImage.getTop()
                + (moveFromAvatarImage.getHeight() - moveToAvatarImage.getHeight()) / 2);
        moveToAvatarImage.setScaleX((float) ViewUtils.getWidthExcludingPadding(moveFromAvatarImage)
                / ViewUtils.getWidthExcludingPadding(moveToAvatarImage));
        moveToAvatarImage.setScaleY((float) ViewUtils.getHeightExcludingPadding(moveFromAvatarImage)
                / ViewUtils.getHeightExcludingPadding(moveToAvatarImage));
        changeMoveToAvatarTransform.addTarget(moveToAvatarImage);
        transitionSet.addTransition(changeMoveToAvatarTransform);
    }

    private void resetMoveToAvatarTransform(ImageView moveToAvatarImage) {
        moveToAvatarImage.setTranslationX(0);
        moveToAvatarImage.setTranslationY(0);
        moveToAvatarImage.setScaleX(1);
        moveToAvatarImage.setScaleY(1);
    }

    private void toggleShowingAccountList() {

        if (mListener == null) {
            return;
        }

        mShowingAccountList = !mShowingAccountList;
        mListener.showAccountList(mShowingAccountList);
        mDropDownImage.animate()
                .rotation(mShowingAccountList ? 180 : 0)
                .setDuration(ViewUtils.getShortAnimTime(getContext()))
                .start();
    }

    public interface Adapter {
        User getPartialUser(Account account);
        UserInfo getUserInfo(Account account);
    }

    public interface Listener {
        void openProfile(Account account);
        void showAccountList(boolean show);
        void onActiveAccountChanged(Account newAccount);
    }
}