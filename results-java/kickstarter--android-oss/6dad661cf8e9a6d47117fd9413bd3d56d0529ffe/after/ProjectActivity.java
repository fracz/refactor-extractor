package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.widget.Toast;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.presenters.ProjectPresenter;
import com.kickstarter.ui.adapters.ProjectAdapter;
import com.kickstarter.ui.views.IconTextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;

@RequiresPresenter(ProjectPresenter.class)
public class ProjectActivity extends BaseActivity<ProjectPresenter> {
  @Bind(R.id.rewards_recycler_view) RecyclerView rewardsRecyclerView;
  @Bind(R.id.star_icon) IconTextView starIconTextView;

  @Inject Money money;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.project_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    final Intent intent = getIntent();
    final Project project = intent.getExtras().getParcelable(getString(R.string.intent_project));
    presenter.takeProject(project);
  }

  public void show(final Project project) {
    int starColor = (project.isStarred()) ? R.color.green : R.color.dark_gray;
    starIconTextView.setTextColor(ContextCompat.getColor(this, starColor));
    loadProjectRewards(project, project.rewards());
  }

  public void loadProjectRewards(final Project project, final List<Reward> rewards) {
    if (project.rewards() != null) {
      final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
      final List<Pair<Project, Reward>> projectAndRewards = Observable.from(rewards)
        .map(reward -> Pair.create(project, reward))
        .filter(projectRewardPair -> !projectRewardPair.second.isNoReward())
        .toList().toBlocking().single();

      final ProjectAdapter adapter = new ProjectAdapter(project, projectAndRewards, presenter);
      rewardsRecyclerView.setLayoutManager(layoutManager);
      rewardsRecyclerView.setAdapter(adapter);
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  @OnClick(R.id.back_project_button)
  public void backProjectButtonOnClick() {
    presenter.takeBackProjectClick();
  }

  @OnClick(R.id.star_icon)
  public void starProjectClick() {
    presenter.takeStarClick();
  }

  @OnClick(R.id.share_icon)
  public void shareProject() {
    presenter.takeShareClick();
  }

  public void showProjectDescription(final Project project) {
    startWebViewActivity(project.descriptionUrl());
  }

  public void showCreatorBio(final Project project) {
    startWebViewActivity(project.creatorBioUrl());
  }

  public void showUpdates(final Project project) {
    startWebViewActivity(project.updatesUrl());
  }

  public void showStarPrompt() {
    final Toast toast = Toast.makeText(this, R.string.Well_remind_you_48_hours, Toast.LENGTH_LONG);
    toast.show();
  }

  public void startCheckoutActivity(final Project project) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(getString(R.string.intent_project), project)
      .putExtra(getString(R.string.intent_url), project.newPledgeUrl());
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startCommentsActivity(final Project project) {
    final Intent intent = new Intent(this, CommentFeedActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  // todo: limit the apps you can share to
  public void startShareIntent(final Project project) {
    final Intent intent = new Intent(Intent.ACTION_SEND)
      .setType(getString(R.string.intent_share_type))
      .putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_message), project.name(), project.webProjectUrl()));
    startActivity(intent);
  }

  private void startWebViewActivity(final String url) {
    final Intent intent = new Intent(this, DisplayWebViewActivity.class)
      .putExtra(getString(R.string.intent_url), url);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startLoginToutActivity() {
    Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_forward), true);
    startActivityForResult(intent, ActivityRequestCodes.PROJECT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
    if (resultCode != RESULT_OK) {
      finish();
    } else {
      presenter.takeLoginSuccess();
    }
  }
}