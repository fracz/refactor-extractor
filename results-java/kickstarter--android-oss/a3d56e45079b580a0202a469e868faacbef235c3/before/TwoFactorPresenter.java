package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.ApiErrorTransformer;
import com.kickstarter.libs.rx.transformers.NeverErrorTransformer;
import com.kickstarter.libs.utils.RxUtils;
import com.kickstarter.presenters.errors.TwoFactorPresenterErrors;
import com.kickstarter.presenters.inputs.TwoFactorPresenterInputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.TwoFactorActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class TwoFactorPresenter extends Presenter<TwoFactorActivity> implements TwoFactorPresenterInputs, TwoFactorPresenterErrors {
  // INPUTS
  private final PublishSubject<String> code = PublishSubject.create();
  private final PublishSubject<View> loginClick = PublishSubject.create();
  private final PublishSubject<View> resendClick = PublishSubject.create();

  // ERRORS
  private final PublishSubject<ErrorEnvelope> tfaError = PublishSubject.create();
  public Observable<String> tfaCodeMismatchError() {
    return tfaError
      .filter(env -> env.ksrCode().equals(ErrorEnvelope.TFA_FAILED))
      .map(env -> env.errorMessages().get(0));
  }
  public Observable<Void> genericTfaError() {
    return tfaError
      .filter(env -> !env.ksrCode().equals(ErrorEnvelope.TFA_FAILED))
      .map(__ -> null);
  }

  @Inject CurrentUser currentUser;
  @Inject ApiClient client;

  public TwoFactorPresenterInputs inputs() {
    return this;
  }
  public TwoFactorPresenterErrors errors() { return this; }

  @Override
  public void code(@NonNull final String s) {
    code.onNext(s);
  }

  @Override
  public void loginClick(@NonNull final View view) {
    loginClick.onNext(view);
  }

  @Override
  public void resendClick(@NonNull final View view) {
    resendClick.onNext(view);
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

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
          viewAndEnvelope -> success(viewAndEnvelope.second, viewAndEnvelope.first)
        )
    );

    addSubscription(
      RxUtils.takeWhen(viewSubject, resendClick)
        .switchMap(view -> resendCode(view.email(), view.password()))
        .observeOn(AndroidSchedulers.mainThread())
          // TODO: It might be a gotcha to have an empty subscription block, but I don't remember
          // why. We should investigate.
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
    currentUser.login(envelope.user(), envelope.accessToken());
    view.onSuccess();
  }

  private Observable<AccessTokenEnvelope> submit(@NonNull final String email, @NonNull final String password,
    @NonNull final String code) {
    return client.login(email, password, code)
      .compose(new ApiErrorTransformer<>(tfaError));
  }

  private Observable<AccessTokenEnvelope> resendCode(@NonNull final String email, @NonNull final String password) {
    return client.login(email, password)
      .compose(new NeverErrorTransformer<>());
  }
}