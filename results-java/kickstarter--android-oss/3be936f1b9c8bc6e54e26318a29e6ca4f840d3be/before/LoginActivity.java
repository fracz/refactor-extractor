package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.presenters.LoginPresenter;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.GenericDialogAlert;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@RequiresPresenter(LoginPresenter.class)
public final class LoginActivity extends BaseActivity<LoginPresenter> {
  @Bind(R.id.email) EditText emailEditText;
  @Bind(R.id.login_button) Button loginButton;
  @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;
  @Bind(R.id.password) EditText passwordEditText;

  @BindString(R.string.Login_does_not_match_any_of_our_records) String loginDoesNotMatchString;
  @BindString(R.string.Unable_to_login) String unableToLoginString;
  @BindString(R.string.Log_in) String loginString;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(loginString);

    final boolean forward = getIntent().getBooleanExtra(getString(R.string.intent_forward), false);

    addSubscription(
      errorMessages()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::displayToast)
    );

    addSubscription(
      presenter.errors.tfaChallenge()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> {
          startTwoFactorActivity(forward);
        })
    );

    addSubscription(
      presenter.outputs.loginSuccess()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> {
          onSuccess(forward);
        })
    );

    final boolean confirmResetPassword = getIntent().getBooleanExtra(getString(R.string.intent_confirm_reset_password), false);
    if (confirmResetPassword) {
      showResetPasswordConfirmation(getIntent().getExtras().getString(getString(R.string.intent_email)));
    }
  }

  private Observable<String> errorMessages() {
    return presenter.errors.invalidLoginError().map(ObjectUtils.coalesceWith(loginDoesNotMatchString))
      .mergeWith(presenter.errors.genericLoginError().map(ObjectUtils.coalesceWith(unableToLoginString)));
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    if (requestCode != ActivityRequestCodes.LOGIN_ACTIVITY_TWO_FACTOR_ACTIVITY_FORWARD) {
      return;
    }

    setResult(resultCode, intent);
    finish();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  @OnTextChanged(R.id.email)
  void onEmailTextChanged(@NonNull final CharSequence email) {
    presenter.inputs.email(email.toString());
  }

  @OnTextChanged(R.id.password)
  void onPasswordTextChanged(@NonNull final CharSequence password) {
    presenter.inputs.password(password.toString());
  }

  @OnClick(R.id.forgot_your_password_text_view)
  public void forgotYourPasswordTextViewClick() {
    final Intent intent = new Intent(this, ForgotPasswordActivity.class);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.login_button)
  public void loginButtonOnClick() {
    presenter.inputs.loginClick();
  }

  public void onSuccess(final boolean forward) {
    if (forward) {
      setResult(Activity.RESULT_OK);
      finish();
    } else {
      final Intent intent = new Intent(this, DiscoveryActivity.class)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
    }
  }

  public void setFormEnabled(final boolean enabled) {
    loginButton.setEnabled(enabled);
  }

  public void startTwoFactorActivity(final boolean forward) {
    final Intent intent = new Intent(this, TwoFactorActivity.class)
      .putExtra(getString(R.string.intent_email), emailEditText.getText().toString())
      .putExtra(getString(R.string.intent_password), passwordEditText.getText().toString())
      .putExtra(getString(R.string.intent_forward), forward);
    if (forward) {
      startActivityForResult(intent, ActivityRequestCodes.LOGIN_ACTIVITY_TWO_FACTOR_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
    }
  }

  private void showResetPasswordConfirmation(@NonNull final String email) {
    final String message = getResources().getString(R.string.We_sent_an_email_to_email_with_instructions_to_reset_your_password, email);
    final GenericDialogAlert alert = new GenericDialogAlert(this);
    alert.show();
    alert.setMessage(message);
  }
}