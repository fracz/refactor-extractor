package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiErrorHandler;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.ApiError;
import com.kickstarter.services.ApiResponses.AccessTokenEnvelope;
import com.kickstarter.ui.activities.TwoFactorActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.subjects.PublishSubject;

public class TwoFactorPresenter extends Presenter<TwoFactorActivity> {
  @Inject CurrentUser currentUser;
  @Inject ApiClient client;
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Void> resendClick = PublishSubject.create();

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KsrApplication) context.getApplicationContext()).component().inject(this);

    final Observable<String> code = viewSubject
      .flatMap(v -> WidgetObservable.text(v.code))
      .map(v -> v.text().toString());

    final Observable<Boolean> isValid = code
      .map(TwoFactorPresenter::isValid);

    final Observable<Pair<TwoFactorActivity, String>> viewAndCode =
      RxUtils.combineLatestPair(viewSubject, code);

    final Observable<AccessTokenEnvelope> tokenEnvelope = RxUtils.takeWhen(viewAndCode, loginClick)
      .switchMap(vc -> submit(vc.first.email(), vc.first.password(), vc.second));

    addSubscription(
      RxUtils.combineLatestPair(viewSubject, isValid)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(viewAndValid -> viewAndValid.first.setLoginEnabled(viewAndValid.second))
    );

    addSubscription(
      RxUtils.combineLatestPair(viewSubject, tokenEnvelope)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          viewAndEnvelope -> success(viewAndEnvelope.second, viewAndEnvelope.first),
          this::error
        )
    );

    addSubscription(
      RxUtils.takeWhen(viewSubject, resendClick)
        .switchMap(view -> resendCode(view.email(), view.password()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe()
    );
  }

  private static boolean isValid(@NonNull final String code) {
    return code.length() > 0;
  }

  public void takeLoginClick() {
    loginClick.onNext(null);
  }

  public void takeResendClick() {
    resendClick.onNext(null);
  }

  private void success(@NonNull final AccessTokenEnvelope envelope, @NonNull final TwoFactorActivity view) {
    currentUser.login(envelope.user, envelope.access_token);
    view.onSuccess();
  }

  private void error(final Throwable e) {
    if (!hasView()) {
      return;
    }

    new ApiErrorHandler(e, view()) {
      @Override
      public void handleApiError(final ApiError api_error) {
        switch (api_error.errorEnvelope().ksrCode()) {
          case TFA_FAILED:
            displayError(R.string.The_code_provided_does_not_match);
            break;
          default:
            displayError(R.string.Unable_to_login);
            break;
        }
      }
    }.handleError();
  }

  private Observable<AccessTokenEnvelope> submit(final String email, final String password, final String code) {
    return client.login(email, password, code);
  }

  private Observable<AccessTokenEnvelope> resendCode(final String email, final String password) {
    return client.login(email, password);
  }
}