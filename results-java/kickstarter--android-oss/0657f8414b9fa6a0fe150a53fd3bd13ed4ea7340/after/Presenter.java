package com.kickstarter.libs;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class Presenter<ViewType> {
  private ViewType view;
  protected final PublishSubject<ViewType> viewSubject = PublishSubject.create();
  private final List<Subscription> subscriptions = new ArrayList<>();

  protected void onCreate(Bundle savedInstanceState) {
    Timber.d("onCreate %s", this.toString());
    onTakeView(null);
  }

  protected void onResume(ViewType view) {
    Timber.d("onResume %s", this.toString());
    onTakeView(view);
  }

  protected void onPause() {
    Timber.d("onPause %s", this.toString());
    onTakeView(null);
  }

  protected void onDestroy() {
    Timber.d("onDestroy %s", this.toString());
    for (Subscription subscription : subscriptions) {
      subscription.unsubscribe();
    }

    viewSubject.onCompleted();
  }

  protected void onTakeView(ViewType view) {
    Timber.d("onTakeView %s %s", this.toString(), (view != null ? view.toString() : "null"));
    this.view = view;
    viewSubject.onNext(view);
  }

  protected ViewType view() {
    return this.view;
  }

  protected boolean hasView() {
    return this.view != null;
  }

  public void addSubscription (Subscription subscription) {
    subscriptions.add(subscription);
  }

  public final <T> Subscription subscribeTo(final Observable<T> ob, final Action1<? super T> onNext) {
    Subscription s = ob.subscribe(onNext);
    subscriptions.add(s);
    return s;
  }

  public final <T> Subscription subscribeTo(final Observable<T> ob, final Action1<? super T> onNext, final Action1<Throwable> onError) {
    Subscription s = ob.subscribe(onNext, onError);
    subscriptions.add(s);
    return s;
  }

  public final <T> Subscription subscribeTo(final Observable<T> ob, final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onComplete) {
    Subscription s = ob.subscribe(onNext, onError, onComplete);
    subscriptions.add(s);
    return s;
  }

  public void save(Bundle state) {
    Timber.d("save %s", this.toString());
    // TODO
  }
}