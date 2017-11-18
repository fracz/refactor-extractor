package com.kickstarter.viewmodels;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.LoginToutActivity;
import com.kickstarter.viewmodels.errors.LoginToutViewModelErrors;
import com.kickstarter.viewmodels.inputs.LoginToutViewModelInputs;
import com.kickstarter.viewmodels.outputs.LoginToutViewModelOutputs;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class LoginToutViewModel extends ViewModel<LoginToutActivity> implements LoginToutViewModelInputs,
  LoginToutViewModelOutputs, LoginToutViewModelErrors {

  protected final class ActivityResultData {
    final int requestCode;
    final int resultCode;
    final @NonNull Intent intent;

    protected ActivityResultData(final int requestCode, final int resultCode, final @NonNull Intent intent) {
      this.requestCode = requestCode;
      this.resultCode = resultCode;
      this.intent = intent;
    }
  }

  // INPUTS
  private final PublishSubject<ActivityResultData> activityResult = PublishSubject.create();
  private final PublishSubject<String> facebookAccessToken = PublishSubject.create();
  private final PublishSubject<String> reason = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> facebookLoginSuccess = PublishSubject.create();
  public final Observable<Void> facebookLoginSuccess() {
    return facebookLoginSuccess.asObservable();
  }

  // ERRORS
  private final PublishSubject<FacebookException> facebookAuthorizationError = PublishSubject.create();
  public final Observable<String> facebookAuthorizationError() {
    return facebookAuthorizationError
      .map(FacebookException::getLocalizedMessage);
  }

  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();
  public final Observable<ErrorEnvelope.FacebookUser> confirmFacebookSignupError() {
   return loginError
     .filter(ErrorEnvelope::isConfirmFacebookSignupError)
     .map(ErrorEnvelope::facebookUser);
  }

  public final Observable<String> missingFacebookEmailError() {
    return loginError
      .filter(ErrorEnvelope::isMissingFacebookEmailError)
      .map(ErrorEnvelope::errorMessage);
  }

  public final Observable<String> facebookInvalidAccessTokenError() {
    return loginError
      .filter(ErrorEnvelope::isFacebookInvalidAccessTokenError)
      .map(ErrorEnvelope::errorMessage);
  }

  public final Observable<Void> tfaChallenge() {
    return loginError
      .filter(ErrorEnvelope::isTfaRequiredError)
      .map(__ -> null);
  }

  protected @Inject CurrentUser currentUser;
  protected @Inject ApiClientType client;

  public final LoginToutViewModelInputs inputs = this;
  public final LoginToutViewModelOutputs outputs = this;
  public final LoginToutViewModelErrors errors = this;

  public LoginToutViewModel() {
    final CallbackManager callbackManager = CallbackManager.Factory.create();
    LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
      @Override
      public void onSuccess(final @NonNull LoginResult result) {
        facebookAccessToken.onNext(result.getAccessToken().getToken());
      }

      @Override
      public void onCancel() {
        // continue
      }

      @Override
      public void onError(final @NonNull FacebookException error) {
        if (error instanceof FacebookAuthorizationException) {
          facebookAuthorizationError.onNext(error);
        }
      }
    });

    activityResult
      .compose(bindToLifecycle())
      .subscribe(r -> callbackManager.onActivityResult(r.requestCode, r.resultCode, r.intent));

    facebookAccessToken
      .switchMap(this::loginWithFacebookAccessToken)
      .compose(bindToLifecycle())
      .subscribe(this::facebookLoginSuccess);

    facebookAuthorizationError
      .compose(bindToLifecycle())
      .subscribe(this::clearFacebookSession);
  }

  @Override
  protected void onCreate(final @NonNull Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    reason
      .take(1)
      .compose(bindToLifecycle())
      .subscribe(koala::trackLoginRegisterTout);

    loginError
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackLoginError());

    facebookLoginSuccess
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackFacebookLoginSuccess());

    missingFacebookEmailError()
      .mergeWith(facebookInvalidAccessTokenError())
      .mergeWith(facebookAuthorizationError())
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackFacebookLoginError());
  }

  @Override
  public void activityResult(final int requestCode, final int resultCode, final @NonNull Intent intent) {
    final ActivityResultData activityResultData = new ActivityResultData(requestCode, resultCode, intent);
    activityResult.onNext(activityResultData);
  }

  public void clearFacebookSession(final @NonNull FacebookException e) {
    LoginManager.getInstance().logOut();
  }

  @Override
  public void facebookLoginClick(final @NonNull LoginToutActivity activity, @NonNull List<String> facebookPermissions) {
    LoginManager.getInstance().logInWithReadPermissions(activity, facebookPermissions);
  }

  public void facebookLoginSuccess(final @NonNull AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    facebookLoginSuccess.onNext(null);
  }

  private Observable<AccessTokenEnvelope> loginWithFacebookAccessToken(final @NonNull String fbAccessToken) {
    return client.loginWithFacebook(fbAccessToken)
      .compose(Transformers.pipeApiErrorsTo(loginError))
      .compose(Transformers.neverError());
  }

  @Override
  public void reason(final @Nullable String r) {
    reason.onNext(r);
  }
}