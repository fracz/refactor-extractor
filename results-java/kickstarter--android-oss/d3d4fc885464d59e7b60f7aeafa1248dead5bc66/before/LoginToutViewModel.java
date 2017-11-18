package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.LoginToutActivity;
import com.kickstarter.ui.data.ActivityResult;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.viewmodels.inputs.LoginToutViewModelInputs;
import com.kickstarter.viewmodels.outputs.LoginToutViewModelOutputs;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.pipeApiErrorsTo;

public final class LoginToutViewModel extends ActivityViewModel<LoginToutActivity> implements LoginToutViewModelInputs,
  LoginToutViewModelOutputs {
  private CallbackManager callbackManager;
  private final CurrentUserType currentUser;
  private final ApiClientType client;

  public LoginToutViewModel(final @NonNull Environment environment) {
    super(environment);

    this.client = environment.apiClient();
    this.currentUser = environment.currentUser();

    registerFacebookCallback();

    final Observable<AccessTokenEnvelope> facebookSuccessTokenEnvelope = this.facebookAccessToken
      .switchMap(this::loginWithFacebookAccessToken)
      .share();

    intent()
      .map(i -> i.getSerializableExtra(IntentKey.LOGIN_REASON))
      .ofType(LoginReason.class)
      .compose(bindToLifecycle())
      .subscribe(this.loginReason::onNext);

    activityResult()
      .compose(bindToLifecycle())
      .subscribe(r -> this.callbackManager.onActivityResult(r.requestCode(), r.resultCode(), r.intent()));

    activityResult()
      .filter(r -> r.isRequestCode(ActivityRequestCodes.LOGIN_FLOW))
      .filter(ActivityResult::isOk)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.finishWithSuccessfulResult.onNext(null));

    this.facebookAuthorizationError
      .compose(bindToLifecycle())
      .subscribe(this::clearFacebookSession);

    facebookSuccessTokenEnvelope
      .compose(bindToLifecycle())
      .subscribe(envelope -> {
        this.currentUser.login(envelope.user(), envelope.accessToken());
        this.finishWithSuccessfulResult.onNext(null);
      });

    this.startFacebookConfirmationActivity = this.loginError
      .filter(ErrorEnvelope::isConfirmFacebookSignupError)
      .map(ErrorEnvelope::facebookUser)
      .compose(combineLatestPair(this.facebookAccessToken));

    this.startLoginActivity = this.loginClick;
    this.startSignupActivity = this.signupClick;

    this.loginReason.take(1)
      .compose(bindToLifecycle())
      .subscribe(this.koala::trackLoginRegisterTout);

    this.loginError
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackLoginError());

    showMissingFacebookEmailErrorToast()
      .mergeWith(showFacebookInvalidAccessTokenErrorToast())
      .mergeWith(showFacebookAuthorizationErrorDialog())
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackFacebookLoginError());
  }

  private void clearFacebookSession(final @NonNull FacebookException e) {
    LoginManager.getInstance().logOut();
  }

  private @NonNull Observable<AccessTokenEnvelope> loginWithFacebookAccessToken(final @NonNull String fbAccessToken) {
    return this.client.loginWithFacebook(fbAccessToken)
      .compose(pipeApiErrorsTo(this.loginError))
      .compose(neverError());
  }

  private void registerFacebookCallback() {
    this.callbackManager = CallbackManager.Factory.create();
    LoginManager.getInstance().registerCallback(this.callbackManager, new FacebookCallback<LoginResult>() {
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
  }

  private final PublishSubject<String> facebookAccessToken = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();
  private final PublishSubject<LoginReason> loginReason = PublishSubject.create();
  private final PublishSubject<Void> signupClick = PublishSubject.create();

  private final BehaviorSubject<FacebookException> facebookAuthorizationError = BehaviorSubject.create();
  private final BehaviorSubject<Void> finishWithSuccessfulResult = BehaviorSubject.create();
  private final Observable<Pair<ErrorEnvelope.FacebookUser, String>> startFacebookConfirmationActivity;
  private final Observable<Void> startLoginActivity;
  private final Observable<Void> startSignupActivity;

  public final LoginToutViewModelInputs inputs = this;
  public final LoginToutViewModelOutputs outputs = this;

  @Override public void facebookLoginClick(final @NonNull LoginToutActivity activity, final @NonNull List<String> facebookPermissions) {
    LoginManager.getInstance().logInWithReadPermissions(activity, facebookPermissions);
  }
  @Override public void loginClick() {
    this.loginClick.onNext(null);
  }
  @Override public void signupClick() {
    this.signupClick.onNext(null);
  }

  @Override public @NonNull Observable<Void> finishWithSuccessfulResult() {
    return this.finishWithSuccessfulResult;
  }
  @Override public @NonNull Observable<String> showFacebookAuthorizationErrorDialog() {
    return this.facebookAuthorizationError
      .map(FacebookException::getLocalizedMessage);
  }
  @Override public @NonNull Observable<String> showFacebookInvalidAccessTokenErrorToast() {
    return this.loginError
      .filter(ErrorEnvelope::isFacebookInvalidAccessTokenError)
      .map(ErrorEnvelope::errorMessage);
  }
  @Override public @NonNull Observable<String> showMissingFacebookEmailErrorToast() {
    return this.loginError
      .filter(ErrorEnvelope::isMissingFacebookEmailError)
      .map(ErrorEnvelope::errorMessage);
  }
  @Override public @NonNull Observable<String> showUnauthorizedErrorDialog() {
    return this.loginError
      .filter(ErrorEnvelope::isUnauthorizedError)
      .map(ErrorEnvelope::errorMessage);
  }
  @Override public @NonNull Observable<Pair<ErrorEnvelope.FacebookUser, String>> startFacebookConfirmationActivity() {
    return this.startFacebookConfirmationActivity;
  }
  @Override public @NonNull Observable<Void> startLoginActivity() {
    return this.startLoginActivity;
  }
  @Override public @NonNull Observable<Void> startSignupActivity() {
    return this.startSignupActivity;
  }
  @Override public @NonNull Observable<Void> startTwoFactorChallenge() {
    return this.loginError
      .filter(ErrorEnvelope::isTfaRequiredError)
      .map(__ -> null);
  }
}