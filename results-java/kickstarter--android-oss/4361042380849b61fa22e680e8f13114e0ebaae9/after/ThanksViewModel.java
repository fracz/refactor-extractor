package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.ThanksActivity;
import com.kickstarter.ui.adapters.ThanksAdapter;
import com.kickstarter.ui.viewholders.CategoryPromoViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardMiniViewHolder;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public final class ThanksViewModel extends ViewModel<ThanksActivity> implements ThanksAdapter.Delegate {
  protected @Inject ApiClientType apiClient;

  private final PublishSubject<Void> facebookClick = PublishSubject.create();
  private final PublishSubject<Void> shareClick = PublishSubject.create();
  private final PublishSubject<Void> twitterClick = PublishSubject.create();
  private final PublishSubject<Project> projectCardMiniClick = PublishSubject.create();
  private final PublishSubject<Category> categoryPromoClick = PublishSubject.create();

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    shareClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowShareSheet());

    twitterClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowTwitterShareView());

    facebookClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowFacebookShareView());

    projectCardMiniClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutFinishJumpToProject());
  }

  public void takeProject(final @NonNull Project project) {
    final Observable<Pair<ThanksActivity, Project>> viewAndProject = view
      .compose(Transformers.combineLatestPair(Observable.just(project)))
      .filter(vp -> vp.first != null);

    viewAndProject
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.show(vp.second));

    viewAndProject
      .compose(Transformers.takeWhen(facebookClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startFacebookShareIntent(vp.second));

    viewAndProject
      .compose(Transformers.takeWhen(shareClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startShareIntent(vp.second));

    viewAndProject
      .compose(Transformers.takeWhen(twitterClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startTwitterShareIntent(vp.second));

    viewChange
      .compose(Transformers.takePairWhen(projectCardMiniClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startProjectIntent(vp.second));

    viewChange
      .compose(Transformers.takePairWhen(categoryPromoClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startDiscoveryCategoryIntent(vp.second));

    final Category projectCategory = project.category();
    if (projectCategory != null) {
      final Observable<Category> rootCategory = apiClient.fetchCategory(String.valueOf(projectCategory.rootId()))
        .compose(Transformers.neverError());

      final Observable<Pair<List<Project>, Category>> projectsAndRootCategory = moreProjects(project)
        .compose(Transformers.zipPair(rootCategory));

      view
        .compose(Transformers.combineLatestPair(projectsAndRootCategory))
        .observeOn(AndroidSchedulers.mainThread())
        .compose(bindToLifecycle())
        .subscribe(vpc -> {
          final ThanksActivity view = vpc.first;
          final List<Project> ps = vpc.second.first;
          final Category category = vpc.second.second;
          view.showRecommended(ps, category);
        });
    }

    categoryPromoClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutFinishJumpToDiscovery());
    projectCardMiniClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutFinishJumpToProject());
  }

  /**
   * Returns a shuffled list of 3 recommended projects, with fallbacks to similar and staff picked projects
   * for users with fewer than 3 recommendations.
   */
  public Observable<List<Project>> moreProjects(final @NonNull Project project) {
    final DiscoveryParams recommendedParams = DiscoveryParams.builder()
      .backed(-1)
      .recommended(true)
      .perPage(6)
      .build();

    final DiscoveryParams similarToParams = DiscoveryParams.builder()
      .backed(-1)
      .similarTo(project)
      .perPage(3)
      .build();

    final Category category = project.category();
    final DiscoveryParams staffPickParams = DiscoveryParams.builder()
      .category(category == null ? null : category.root())
      .backed(-1)
      .staffPicks(true)
      .perPage(3)
      .build();

    final Observable<Project> recommendedProjects = apiClient.fetchProjects(recommendedParams)
      .retry(2)
      .map(DiscoverEnvelope::projects)
      .map(ListUtils::shuffle)
      .flatMap(Observable::from)
      .take(3);

    final Observable<Project> similarToProjects = apiClient.fetchProjects(similarToParams)
      .retry(2)
      .map(DiscoverEnvelope::projects)
      .flatMap(Observable::from);

    final Observable<Project> staffPickProjects = apiClient.fetchProjects(staffPickParams)
      .retry(2)
      .map(DiscoverEnvelope::projects)
      .flatMap(Observable::from);

    return Observable.concat(recommendedProjects, similarToProjects, staffPickProjects)
      .compose(Transformers.neverError())
      .distinct()
      .take(3)
      .toList();
  }

  public void takeFacebookClick() {
    facebookClick.onNext(null);
  }

  public void takeShareClick() {
    shareClick.onNext(null);
  }

  public void takeTwitterClick() {
    twitterClick.onNext(null);
  }

  @Override
  public void categoryPromoClick(final @NonNull CategoryPromoViewHolder viewHolder, final @NonNull Category category) {
    categoryPromoClick.onNext(category);
  }

  @Override
  public void projectCardMiniClick(final @NonNull ProjectCardMiniViewHolder viewHolder, final @NonNull Project project) {
    projectCardMiniClick.onNext(project);
  }
}