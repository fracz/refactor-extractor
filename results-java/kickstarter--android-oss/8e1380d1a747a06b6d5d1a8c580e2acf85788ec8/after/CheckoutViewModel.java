package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.gson.Gson;
import com.kickstarter.libs.AndroidPayCapability;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.models.AndroidPayPayload;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.AndroidPayUtils;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.ui.data.ActivityResult;
import com.kickstarter.viewmodels.errors.CheckoutViewModelErrors;
import com.kickstarter.viewmodels.inputs.CheckoutViewModelInputs;
import com.kickstarter.viewmodels.outputs.CheckoutViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class CheckoutViewModel extends ViewModel<CheckoutActivity> implements CheckoutViewModelInputs,
  CheckoutViewModelOutputs, CheckoutViewModelErrors {
  private AndroidPayCapability androidPayCapability;
  private Gson gson;

  // INPUTS
  private PublishSubject<String> pageIntercepted = PublishSubject.create();
  @Override
  public void pageIntercepted(final @NonNull String str) {
    pageIntercepted.onNext(str);
  }
  private PublishSubject<Void> backButtonClicked = PublishSubject.create();
  public void backButtonClicked() {
    backButtonClicked.onNext(null);
  }
  private PublishSubject<String> payloadString = PublishSubject.create();
  public void takePayloadString(final @Nullable String payloadString) {
    this.payloadString.onNext(payloadString);
  }
  private PublishSubject<Void> confirmAndroidPayClicked = PublishSubject.create();
  public void confirmAndroidPayClicked() {
    confirmAndroidPayClicked.onNext(null);
  }

  // OUTPUTS
  private BehaviorSubject<Project> project = BehaviorSubject.create();
  @Override
  public @NonNull Observable<Project> project() {
    return project;
  }
  private BehaviorSubject<String> title = BehaviorSubject.create();
  @Override
  public @NonNull Observable<String> title() {
    return title;
  }
  private BehaviorSubject<String> url = BehaviorSubject.create();
  @Override
  public @NonNull Observable<String> url() {
    return url;
  }
  private BehaviorSubject<Boolean> displayAndroidPayConfirmation = BehaviorSubject.create();
  public Observable<Boolean> displayAndroidPayConfirmation() {
    return displayAndroidPayConfirmation;
  }
  private BehaviorSubject<Pair<MaskedWallet, AndroidPayPayload>> updateAndroidPayConfirmation = BehaviorSubject.create();
  public Observable<Pair<MaskedWallet, AndroidPayPayload>> updateAndroidPayConfirmation() {
    return updateAndroidPayConfirmation;
  }
  private BehaviorSubject<AndroidPayPayload> showAndroidPaySheet = BehaviorSubject.create();
  public Observable<AndroidPayPayload> showAndroidPaySheet() {
    return showAndroidPaySheet;
  }
  public Observable<Boolean> isAndroidPayAvailable() {
    return Observable.just(androidPayCapability.isCapable());
  }
  private BehaviorSubject<Void> popActivityOffStack = BehaviorSubject.create();
  public Observable<Void> popActivityOffStack() {
    return popActivityOffStack;
  }
  private BehaviorSubject<Pair<MaskedWallet, AndroidPayPayload>> attemptAndroidPayConfirmation = BehaviorSubject.create();
  public Observable<Pair<MaskedWallet, AndroidPayPayload>> attemptAndroidPayConfirmation() {
    return attemptAndroidPayConfirmation;
  }
  private BehaviorSubject<FullWallet> completeAndroidPay = BehaviorSubject.create();
  public Observable<FullWallet> completeAndroidPay() {
    return completeAndroidPay;
  }

  // ERRORS
  final private Observable<Integer> androidPayError;
  public Observable<Integer> androidPayError() {
    return this.androidPayError;
  }

  public final CheckoutViewModelInputs inputs = this;
  public final CheckoutViewModelOutputs outputs = this;
  public final CheckoutViewModelErrors errors = this;

  public CheckoutViewModel(final @NonNull Environment environment) {
    super(environment);

    androidPayCapability = environment.androidPayCapability();
    gson = environment.gson();

    intent()
      .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
      .ofType(Project.class)
      .compose(bindToLifecycle())
      .subscribe(project::onNext);

    intent()
      .map(i -> i.getStringExtra(IntentKey.TOOLBAR_TITLE))
      .ofType(String.class)
      .compose(bindToLifecycle())
      .subscribe(title::onNext);

    intent()
      .map(i -> i.getStringExtra(IntentKey.URL))
      .ofType(String.class)
      .take(1)
      .mergeWith(pageIntercepted)
      .compose(bindToLifecycle())
      .subscribe(url::onNext);

    final Observable<MaskedWallet> maskedWallet = activityResult()
      .filter(AndroidPayUtils::isMaskedWalletRequest)
      .map(ActivityResult::intent)
      .map(i -> i.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET))
      .ofType(MaskedWallet.class);

    final Observable<FullWallet> fullWallet = activityResult()
      .filter(AndroidPayUtils::isFullWalletRequest)
      .map(ActivityResult::intent)
      .map(i -> i.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET))
      .ofType(FullWallet.class);

    this.androidPayError = activityResult()
      .map(AndroidPayUtils::walletRequestError)
      .filter(ObjectUtils::isNotNull);

    final Observable<AndroidPayPayload> payload = payloadString
      .map(str -> AndroidPayUtils.payloadFromString(str, gson))
      .ofType(AndroidPayPayload.class);

    final Observable<Boolean> confirmationVisibilityOnBack = displayAndroidPayConfirmation
      .compose(Transformers.takeWhen(backButtonClicked));

    payload
      .compose(bindToLifecycle())
      .subscribe(showAndroidPaySheet::onNext);

    confirmationVisibilityOnBack
      .filter(BooleanUtils::isFalse)
      .compose(bindToLifecycle())
      .subscribe(__ -> popActivityOffStack.onNext(null));

    confirmationVisibilityOnBack
      .filter(BooleanUtils::isTrue)
      .map(__ -> false)
      .mergeWith(maskedWallet.map(__ -> true))
      .compose(bindToLifecycle())
      .subscribe(displayAndroidPayConfirmation::onNext);

    maskedWallet
      .compose(Transformers.combineLatestPair(payload))
      .filter(wp -> wp.first != null && wp.second != null)
      .compose(bindToLifecycle())
      .subscribe(updateAndroidPayConfirmation::onNext);

    maskedWallet
      .compose(Transformers.combineLatestPair(payload))
      .compose(Transformers.takeWhen(confirmAndroidPayClicked))
      .filter(wp -> wp.second != null)
      .compose(bindToLifecycle())
      .subscribe(attemptAndroidPayConfirmation::onNext);

    fullWallet
      .compose(bindToLifecycle())
      .subscribe(completeAndroidPay::onNext);

    showAndroidPaySheet
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackShowAndroidPaySheet());

    completeAndroidPay
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackAndroidPayFinished());

    // Start by not showing the confirmation page.
    displayAndroidPayConfirmation.onNext(false);
  }
}