package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.ViewPledgeActivity;
import com.kickstarter.viewmodels.errors.ViewPledgeViewModelErrors;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public final class ViewPledgeViewModel extends ViewModel<ViewPledgeActivity> implements ViewPledgeViewModelErrors {
  private final PublishSubject<Project> project = PublishSubject.create();

  protected @Inject ApiClientType client;
  protected @Inject CurrentUser currentUser;

  // Errors
  private PublishSubject<ErrorEnvelope> backingLoadFailed = PublishSubject.create();
  public Observable<Void> backingLoadFailed() {
    return backingLoadFailed.map(__ -> null);
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<Backing> backing = project
      .compose(Transformers.combineLatestPair(currentUser.observable()))
      .filter(pu -> pu.second != null)
      .switchMap(pu -> fetchProjectBacking(pu.first, pu.second))
      .share();

    final Observable<Pair<ViewPledgeActivity, Backing>> viewAndBacking = view
      .compose(Transformers.takePairWhen(backing))
      .share();

    viewAndBacking
      .compose(Transformers.takeWhen(backing))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vb -> vb.first.show(vb.second));
  }

  public void initialize(final @NonNull Project project) {
    this.project.onNext(project);
  }

  public Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user) {
    return client.fetchProjectBacking(project, user)
      .compose(Transformers.pipeApiErrorsTo(backingLoadFailed))
      .compose(Transformers.neverError());
  }
}