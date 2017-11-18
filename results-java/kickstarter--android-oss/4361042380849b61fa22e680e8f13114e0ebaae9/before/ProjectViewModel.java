package com.kickstarter.viewmodels;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfig;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.RefTagUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;
import com.kickstarter.ui.activities.ProjectActivity;
import com.kickstarter.ui.adapters.ProjectAdapter;
import com.kickstarter.ui.viewholders.ProjectViewHolder;
import com.kickstarter.ui.viewholders.RewardViewHolder;
import com.kickstarter.viewmodels.inputs.ProjectViewModelInputs;
import com.kickstarter.viewmodels.outputs.ProjectViewModelOutputs;

import java.net.CookieManager;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ProjectViewModel extends ViewModel<ProjectActivity> implements ProjectAdapter.Delegate,
  ProjectViewModelInputs, ProjectViewModelOutputs {
  protected @Inject ApiClientType client;
  protected @Inject CurrentUser currentUser;
  protected @Inject CookieManager cookieManager;
  protected @Inject CurrentConfig currentConfig;
  protected @Inject SharedPreferences sharedPreferences;

  /**
   * A light-weight value to hold two ref tags and a project. Two ref tags are stored: one comes from parceled
   * data in the activity and the other comes from the ref stored in a cookie associated to the project.
   */
  private class RefTagsAndProject {
    final @Nullable RefTag refTagFromIntent;
    final @Nullable RefTag refTagFromCookie;
    final @NonNull Project project;

    private RefTagsAndProject(final @Nullable RefTag refTagFromIntent, final @Nullable RefTag refTagFromCookie, final @NonNull Project project) {
      this.refTagFromIntent = refTagFromIntent;
      this.refTagFromCookie = refTagFromCookie;
      this.project = project;
    }
  }

  // INPUTS
  private final PublishSubject<Project> initializer = PublishSubject.create();
  public void initializer(final @NonNull Project project) {
    initializer.onNext(project);
  }
  private final PublishSubject<Void> backProjectClicked = PublishSubject.create();
  public void backProjectClicked() {
    this.backProjectClicked.onNext(null);
  }
  private final PublishSubject<Void> shareClicked = PublishSubject.create();
  public void shareClicked() {
    this.shareClicked.onNext(null);
  }
  private final PublishSubject<Void> blurbClicked = PublishSubject.create();
  public void blurbClicked() {
    this.blurbClicked.onNext(null);
  }
  private final PublishSubject<Void> commentsClicked = PublishSubject.create();
  public void commentsClicked() {
    this.commentsClicked.onNext(null);
  }
  private final PublishSubject<Void> creatorNameClicked = PublishSubject.create();
  public void creatorNameClicked() {
    this.creatorNameClicked.onNext(null);
  }
  private final PublishSubject<Void> managePledgeClicked = PublishSubject.create();
  public void managePledgeClicked() {
    this.managePledgeClicked.onNext(null);
  }
  private final PublishSubject<Void> updatesClicked = PublishSubject.create();
  public void updatesClicked() {
    this.updatesClicked.onNext(null);
  }
  private final PublishSubject<Void> playVideoClicked = PublishSubject.create();
  public void playVideoClicked() {
    this.playVideoClicked.onNext(null);
  }
  private final PublishSubject<Void> viewPledgeClicked = PublishSubject.create();
  public void viewPledgeClicked() {
    this.viewPledgeClicked.onNext(null);
  }
  private final PublishSubject<Void> starClicked = PublishSubject.create();
  public void starClicked() {
    this.starClicked.onNext(null);
  }
  private final PublishSubject<Reward> rewardClicked = PublishSubject.create();
  public void rewardClicked(final @NonNull Reward reward) {
    this.rewardClicked.onNext(reward);
  }
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  public void loginSuccess() {
    this.loginSuccess.onNext(null);
  }
  private final PublishSubject<RefTag> intentRefTag = PublishSubject.create();
  public void intentRefTag(final @Nullable RefTag refTag) {
    intentRefTag.onNext(refTag);
  }
  final PublishSubject<PushNotificationEnvelope> pushNotificationEnvelope = PublishSubject.create();
  public void takePushNotificationEnvelope(final @Nullable PushNotificationEnvelope envelope) {
    pushNotificationEnvelope.onNext(envelope);
  }
  public final ProjectViewModelInputs inputs = this;

  // OUTPUTS
  final BehaviorSubject<Project> project = BehaviorSubject.create();
  public final Observable<Pair<Project, Config>> projectAndConfig() {
    return project.compose(Transformers.combineLatestPair(currentConfig.observable()));
  }
  public Observable<Project> showShareSheet() {
    return this.project.compose(Transformers.takeWhen(this.shareClicked));
  }
  public Observable<Project> playVideo() {
    return this.project.compose(Transformers.takeWhen(this.playVideoClicked));
  }
  public Observable<Project> showCampaign() {
    return this.project.compose(Transformers.takeWhen(this.blurbClicked));
  }
  public Observable<Project> showCreator() {
    return this.project.compose(Transformers.takeWhen(this.creatorNameClicked));
  }
  public Observable<Project> showUpdates() {
    return this.project.compose(Transformers.takeWhen(this.updatesClicked));
  }
  public Observable<Project> showComments() {
    return this.project.compose(Transformers.takeWhen(this.commentsClicked));
  }
  public Observable<Project> startCheckout() {
    return this.project.compose(Transformers.takeWhen(this.backProjectClicked));
  }
  public Observable<Project> startManagePledge() {
    return this.project.compose(Transformers.takeWhen(this.managePledgeClicked));
  }
  public Observable<Project> startViewPledge() {
    return this.project.compose(Transformers.takeWhen(this.viewPledgeClicked));
  }
  public Observable<Pair<Project, Reward>> startCheckoutWithReward() {
    return this.project.compose(Transformers.takePairWhen(this.rewardClicked));
  }
  private final PublishSubject<Void> showStarredPrompt = PublishSubject.create();
  public Observable<Void> showStarredPrompt() {
    return this.showStarredPrompt;
  }
  private final PublishSubject<Void> showLoginTout = PublishSubject.create();
  public Observable<Void> showLoginTout() {
    return this.showLoginTout;
  }
  public final ProjectViewModelOutputs outputs = this;

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<User> loggedInUserOnStarClick = currentUser.observable()
      .compose(Transformers.takeWhen(starClicked))
      .filter(u -> u != null);

    final Observable<User> loggedOutUserOnStarClick = currentUser.observable()
      .compose(Transformers.takeWhen(starClicked))
      .filter(u -> u == null);

    final Observable<Project> projectOnUserChangeStar = initializer
      .compose(Transformers.takeWhen(loggedInUserOnStarClick))
      .switchMap(this::toggleProjectStar)
      .share();

    final Observable<Project> starredProjectOnLoginSuccess = initializer
      .compose(Transformers.takeWhen(loginSuccess))
      .take(1)
      .switchMap(this::starProject)
      .share();

    addSubscription(
      initializer
        .mergeWith(projectOnUserChangeStar)
        .mergeWith(starredProjectOnLoginSuccess)
        .subscribe(this.project::onNext)
    );

    addSubscription(
      projectOnUserChangeStar.mergeWith(starredProjectOnLoginSuccess)
        .filter(Project::isStarred)
        .filter(Project::isLive)
        .filter(p -> !p.isApproachingDeadline())
        .subscribe(__ -> this.showStarredPrompt.onNext(null))
    );

    addSubscription(loggedOutUserOnStarClick.subscribe(__ -> this.showLoginTout.onNext(null)));

    addSubscription(shareClicked.subscribe(__ -> koala.trackShowProjectShareSheet()));

    addSubscription(playVideoClicked.subscribe(__ -> koala.trackVideoStart(project.getValue())));

    addSubscription(projectOnUserChangeStar.mergeWith(starredProjectOnLoginSuccess)
      .subscribe(koala::trackProjectStar));

    // An observable of the ref tag stored in the cookie for the project. Can emit `null`.
    final Observable<RefTag> cookieRefTag = project
      .take(1)
      .map(p -> RefTagUtils.storedCookieRefTagForProject(p, cookieManager, sharedPreferences));

    addSubscription(
      Observable.combineLatest(intentRefTag, cookieRefTag, project, RefTagsAndProject::new)
        .take(1)
        .subscribe(data -> {
          // If a cookie hasn't been set for this ref+project then do so.
          if (data.refTagFromCookie == null && data.refTagFromIntent != null) {
            RefTagUtils.storeCookie(data.refTagFromIntent, data.project, cookieManager, sharedPreferences);
          }

          koala.trackProjectShow(
            data.project,
            data.refTagFromIntent,
            RefTagUtils.storedCookieRefTagForProject(data.project, cookieManager, sharedPreferences)
          );
        })
    );

    addSubscription(
      pushNotificationEnvelope
        .filter(ObjectUtils::isNotNull)
        .take(1)
        .subscribe(koala::trackPushNotification)
    );
  }

  public void projectViewHolderBackProjectClicked(final @NonNull ProjectViewHolder viewHolder) {
    this.backProjectClicked();
  }

  public void projectViewHolderBlurbClicked(final @NonNull ProjectViewHolder viewHolder) {
    this.blurbClicked();
  }

  public void projectViewHolderCommentsClicked(final @NonNull ProjectViewHolder viewHolder) {
    this.commentsClicked();
  }

  public void projectViewHolderCreatorClicked(final @NonNull ProjectViewHolder viewHolder){
    this.creatorNameClicked();
  }

  public void projectViewHolderManagePledgeClicked(final @NonNull ProjectViewHolder viewHolder) {
    this.managePledgeClicked();
  }

  public void projectViewHolderVideoStarted(final @NonNull ProjectViewHolder viewHolder) {
    this.playVideoClicked();
  }

  public void projectViewHolderViewPledgeClicked(final @NonNull ProjectViewHolder viewHolder) {
    this.viewPledgeClicked();
  }

  public void projectViewHolderUpdatesClicked(final @NonNull ProjectViewHolder viewHolder) {
    this.updatesClicked();
  }

  public void rewardViewHolderClicked(final @NonNull RewardViewHolder viewHolder, final @NonNull Reward reward) {
    this.rewardClicked(reward);
  }

  public Observable<Project> starProject(final @NonNull Project project) {
    return client.starProject(project)
      .compose(Transformers.neverError());
  }

  public Observable<Project> toggleProjectStar(final @NonNull Project project) {
    return client.toggleProjectStar(project)
      .compose(Transformers.neverError());
  }
}