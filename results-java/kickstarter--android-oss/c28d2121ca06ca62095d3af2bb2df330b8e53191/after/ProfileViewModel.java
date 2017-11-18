package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.ProfileActivity;
import com.kickstarter.ui.adapters.ProfileAdapter;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;
import com.kickstarter.viewmodels.inputs.ProfileViewModelInputs;
import com.kickstarter.viewmodels.outputs.ProfileViewModelOutputs;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ProfileViewModel extends ViewModel<ProfileActivity> implements ProfileAdapter.Delegate, ProfileViewModelInputs, ProfileViewModelOutputs {
  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  // INPUTS
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
  }

  // OUTPUTS
  private final BehaviorSubject<List<Project>> projects = BehaviorSubject.create();
  @Override public Observable<List<Project>> projects() {
    return projects;
  }
  @Override public Observable<User> user() {
    return currentUser.observable();
  }
  private final PublishSubject<Project> showProject = PublishSubject.create();
  @Override
  public Observable<Project> showProject() {
    return showProject;
  }

  public final ProfileViewModelInputs inputs = this;
  public final ProfileViewModelOutputs outputs = this;

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<User> freshUser = client.fetchCurrentUser()
      .retry(2)
      .onErrorResumeNext(e -> Observable.empty());
    freshUser.subscribe(currentUser::refresh);

    final DiscoveryParams params = DiscoveryParams.builder()
      .backed(1)
      .sort(DiscoveryParams.Sort.ENDING_SOON)
      .build();

    final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator = ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
      .nextPage(nextPage)
      .envelopeToListOfData(DiscoverEnvelope::projects)
      .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
      .loadWithParams(__ -> client.fetchProjects(params))
      .loadWithPaginationPath(client::fetchProjects)
      .build();

    addSubscription(paginator.paginatedData.subscribe(projects));

    koala.trackProfileView();
  }

  public void projectCardViewHolderClicked(final @NonNull ProjectCardViewHolder viewHolder, final @NonNull Project project) {
    this.showProject.onNext(project);
  }
}