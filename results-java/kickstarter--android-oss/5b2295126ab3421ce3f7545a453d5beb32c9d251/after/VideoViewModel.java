package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Video;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.VideoActivity;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public interface VideoViewModel {

  interface Outputs {
    /** Emits the video for the player. */
    Observable<String> preparePlayerWithUrl();
  }

  final class ViewModel extends ActivityViewModel<VideoActivity> implements Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
        .ofType(Project.class)
        .filter(ObjectUtils::isNotNull)
        .map(Project::video)
        .filter(ObjectUtils::isNotNull)
        .map(Video::high)
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(this.preparePlayerWithUrl::onNext);
    }

    private final BehaviorSubject<String> preparePlayerWithUrl = BehaviorSubject.create();

    public final Outputs outputs = this;

    @Override public Observable<String> preparePlayerWithUrl() {
      return this.preparePlayerWithUrl;
    }
  }
}