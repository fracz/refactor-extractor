package com.kickstarter.ui.viewholders;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.SocialUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.viewmodels.ProjectCardHolderViewModel;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ViewUtils.getScreenDensity;
import static com.kickstarter.libs.utils.ViewUtils.getScreenWidthDp;

public final class ProjectCardViewHolder extends KSViewHolder {
  private final ProjectCardHolderViewModel.ViewModel viewModel;
  private Delegate delegate;
  private KSString ksString;

  protected @Bind(R.id.backers_count) TextView backersCountTextView;
  protected @Bind(R.id.backing_group) ViewGroup backingViewGroup;
  protected @Bind(R.id.blurb) TextView blurbTextView;
  protected @Bind(R.id.category) TextView categoryTextView;
  protected @Bind(R.id.deadline_countdown) TextView deadlineCountdownTextView;
  protected @Bind(R.id.deadline_countdown_unit) TextView deadlineCountdownUnitTextView;
  protected @Bind(R.id.featured) TextView featuredTextView;
  protected @Bind(R.id.featured_group) ViewGroup featuredViewGroup;
  protected @Bind(R.id.friend_backing_avatar) ImageView friendBackingAvatarImageView;
  protected @Bind(R.id.friend_backing_message) TextView friendBackingMessageTextView;
  protected @Bind(R.id.friend_backing_group) ViewGroup friendBackingViewGroup;
  protected @Bind(R.id.funding_unsuccessful_text_view) TextView fundingUnsuccessfulTextView;
  protected @Nullable @Bind(R.id.land_card_view_group) ViewGroup landCardViewGroup;
  protected @Bind(R.id.name) TextView nameTextView;
  protected @Bind(R.id.percent) TextView percentTextView;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.photo) ImageView photoImageView;
  protected @Bind(R.id.potd_view_group) ViewGroup potdViewGroup;
  protected @Bind(R.id.project_card_view_group) ViewGroup projectCardViewGroup;
  protected @Bind(R.id.project_metadata_view_group) ViewGroup projectMetadataViewGroup;
  protected @Bind(R.id.project_state_view_group) ViewGroup projectStateViewGroup;
  protected @Bind(R.id.starred_view_group) ViewGroup starredViewGroup;
  protected @Bind(R.id.successfully_funded_text_view) TextView successfullyFundedTextView;

  protected @BindDimen(R.dimen.grid_1) int grid1Dimen;
  protected @BindDimen(R.dimen.grid_2) int grid2Dimen;
  protected @BindDimen(R.dimen.grid_3) int grid3Dimen;
  protected @BindDimen(R.dimen.grid_4) int grid4Dimen;

  protected @BindDrawable(R.drawable.gray_gradient) Drawable grayGradientDrawable;

  protected @BindString(R.string.discovery_baseball_card_status_banner_canceled_date) String bannerCanceledDateString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_suspended_date) String bannerSuspendedDateString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_funding_unsuccessful_date) String fundingUnsuccessfulDateString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_successful_date) String bannerSuccessfulDateString;
  protected @BindString(R.string.discovery_baseball_card_metadata_featured_project) String featuredInString;
  protected @BindString(R.string.discovery_baseball_card_stats_pledged_of_goal) String pledgedOfGoalString;

  public interface Delegate {
    void projectCardViewHolderClick(ProjectCardViewHolder viewHolder, Project project);
  }

  public ProjectCardViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.ksString = environment().ksString();
    this.viewModel = new ProjectCardHolderViewModel.ViewModel(environment());

    ButterKnife.bind(this, view);

    this.viewModel.outputs.backersCountTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.backersCountTextView::setText);

    this.viewModel.outputs.backingViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.backingViewGroup));

    this.viewModel.outputs.blurbText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.blurbTextView::setText);

    this.viewModel.outputs.categoryNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.categoryTextView::setText);

    this.viewModel.outputs.deadlineCountdownText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.deadlineCountdownTextView::setText);

    this.viewModel.outputs.featuredViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.featuredViewGroup));

    this.viewModel.outputs.friendAvatarUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setFriendAvatarUrl);

    this.viewModel.outputs.friendBackingViewIsHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.friendBackingViewGroup));

    this.viewModel.outputs.friendsForNamepile()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(friends ->
        friendBackingMessageTextView.setText(SocialUtils.projectCardFriendNamepile(friends, ksString))
      );

    this.viewModel.outputs.fundingUnsuccessfulTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(fundingUnsuccessfulTextView));

    this.viewModel.outputs.imageIsInvisible()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setInvisible(this.photoImageView));

    this.viewModel.outputs.nameText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(nameTextView::setText);

    this.viewModel.outputs.notifyDelegateOfProjectClick()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(p -> delegate.projectCardViewHolderClick(this, p));

    this.viewModel.outputs.percentageFundedProgressBarIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(percentageFundedProgressBar));

    this.viewModel.outputs.percentageFundedTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percentTextView::setText);

    this.viewModel.outputs.potdViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.potdViewGroup));

    this.viewModel.outputs.percentageFunded()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percentageFundedProgressBar::setProgress);

    this.viewModel.outputs.photoUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::resizeProjectImage);

    this.viewModel.outputs.projectCanceledAt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setCanceledTextView);

    this.viewModel.outputs.projectFailedAt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setFailedAtTextView);

    this.viewModel.outputs.projectForDeadlineCountdownDetail()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setDeadlineCountdownText);

    this.viewModel.outputs.projectStateViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectStateViewGroup));

    this.viewModel.outputs.projectSuccessfulAt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setSuccessfullyFundedTextView);

    this.viewModel.outputs.projectSuspendedAt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setSuspendedAtTextView);

    this.viewModel.outputs.rootCategoryNameForFeatured()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(rootCategory ->
        this.featuredTextView.setText(this.ksString.format(this.featuredInString, "category_name", rootCategory))
      );

    this.viewModel.outputs.metadataViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectMetadataViewGroup));

    this.viewModel.outputs.starredViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.starredViewGroup));

    this.viewModel.outputs.successfullyFundedTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.successfullyFundedTextView));

    this.viewModel.outputs.setDefaultTopPadding()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setDefaultTopPadding);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Project project = ObjectUtils.requireNonNull((Project) data);
    this.viewModel.inputs.configureWith(project);
  }

  private void resizeProjectImage(final @Nullable String avatarUrl) {
    final int targetImageWidth = (int) (getScreenWidthDp(context()) * getScreenDensity(context()) - grid4Dimen);
    final int targetImageHeight = ProjectUtils.photoHeightFromWidthRatio(targetImageWidth);
    photoImageView.setMaxHeight(targetImageHeight);

    Picasso.with(this.context())
      .load(avatarUrl)
      .resize(targetImageWidth, targetImageHeight)  // required to fit properly into apis < 18
      .centerCrop()
      .placeholder(grayGradientDrawable)
      .into(photoImageView);
  }

  private void setDeadlineCountdownText(final @NonNull Project project) {
    deadlineCountdownUnitTextView.setText(ProjectUtils.deadlineCountdownDetail(project, context(), ksString));
  }

  private void setFriendAvatarUrl(final @NonNull String avatarUrl) {
    Picasso.with(context()).load(avatarUrl)
      .transform(new CircleTransformation())
      .into(friendBackingAvatarImageView);
  }

  private void setDefaultTopPadding(final boolean setDefaultPadding) {
    if (setDefaultPadding) {
      adjustLandscapeTopPadding(landCardViewGroup, grid2Dimen, grid2Dimen, grid2Dimen, grid2Dimen);
      adjustViewGroupTopMargin(projectCardViewGroup, 0);
    } else {
      adjustLandscapeTopPadding(landCardViewGroup, grid2Dimen, grid3Dimen, grid2Dimen, grid2Dimen);
      adjustViewGroupTopMargin(projectCardViewGroup, grid1Dimen);
    }
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.viewModel.inputs.projectClicked();
  }

  /**
   *  Adjust spacing between cards when metadata label is present.
   */
  private void adjustViewGroupTopMargin(final @NonNull ViewGroup viewGroup, final int topMargin) {
    final RelativeLayout.MarginLayoutParams marginParams = new RelativeLayout.MarginLayoutParams(
      viewGroup.getLayoutParams()
    );

    marginParams.setMargins(0, topMargin, 0, 0);
    viewGroup.setLayoutParams(marginParams);
  }

  /**
   * Adjust card content spacing when metadata label is present.
   */
  private void adjustLandscapeTopPadding(final @Nullable ViewGroup landscapeViewGroup, final int left, final int top,
    final int right, final int bottom) {
    if (landscapeViewGroup != null) {
      landscapeViewGroup.setPadding(left, top, right, bottom);
    }
  }

  private void setCanceledTextView(final @NonNull DateTime projectCanceledAt) {
    fundingUnsuccessfulTextView.setText(ksString.format(bannerCanceledDateString,
      "date", DateTimeUtils.relative(context(), ksString, projectCanceledAt)
    ));
  }

  private void setSuccessfullyFundedTextView(final @NonNull DateTime projectSuccessfulAt) {
    successfullyFundedTextView.setText(ksString.format(bannerSuccessfulDateString,
      "date", DateTimeUtils.relative(context(), ksString, projectSuccessfulAt)
    ));
  }

  private void setFailedAtTextView(final @NonNull DateTime projectFailedAt) {
    fundingUnsuccessfulTextView.setText(ksString.format(fundingUnsuccessfulDateString,
      "date", DateTimeUtils.relative(context(), ksString, projectFailedAt)
    ));
  }

  private void setSuspendedAtTextView(final @NonNull DateTime projectSuspendedAt) {
    fundingUnsuccessfulTextView.setText(ksString.format(bannerSuspendedDateString,
      "date", DateTimeUtils.relative(context(), ksString, projectSuspendedAt)
    ));
  }
}