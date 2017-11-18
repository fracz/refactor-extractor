package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Paginator;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.presenters.ProfilePresenter;
import com.kickstarter.ui.adapters.ProfileAdapter;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresPresenter(ProfilePresenter.class)
public final class ProfileActivity extends BaseActivity<ProfilePresenter> implements ProfileAdapter.Delegate {
  private ProfileAdapter adapter;
  private final List<Project> projects = new ArrayList<>();
  private Paginator paginator;

  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.user_name) TextView userNameTextView;
  protected @Bind(R.id.created_num) TextView createdNumTextView;
  protected @Bind(R.id.backed_num) TextView backedNumTextView;
  protected @Bind(R.id.created) TextView createdTextView;
  protected @Bind(R.id.backed) TextView backedTextView;
  protected @Bind(R.id.divider_left) View dividerLeftView;
  public @Bind(R.id.recycler_view) RecyclerView recyclerView;

  @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.profile_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    adapter = new ProfileAdapter(projects, this);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    paginator = new Paginator(recyclerView, presenter.inputs::nextPage);

    presenter.outputs.user()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setViews);

    presenter.outputs.projects()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::loadProjects);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  private void loadProjects(@NonNull final List<Project> newProjects) {
    projects.clear();
    projects.addAll(newProjects);
    adapter.notifyDataSetChanged();
  }

  private void setViews(@NonNull final User user) {
    Picasso.with(this).load(user.avatar()
      .medium())
      .transform(new CircleTransformation())
      .into(avatarImageView);

    userNameTextView.setText(user.name());

    final Integer createdNum = user.createdProjectsCount();
    if (createdNum == null || createdNum == 0) {
      createdTextView.setVisibility(View.GONE);
      createdNumTextView.setVisibility(View.GONE);
      dividerLeftView.setVisibility(View.GONE);
    } else {
      createdNumTextView.setText(createdNum.toString());
    }

    final Integer backedNum = user.backedProjectsCount();
    if (backedNum == null || backedNum == 0) {
      backedTextView.setVisibility(View.GONE);
      backedNumTextView.setVisibility(View.GONE);
    } else {
      backedNumTextView.setText(backedNum.toString());
    }
  }

  /** ProjectCardViewHolder Delegate
   * *
   * @param viewHolder
   * @param project
   */
  public void projectCardClick(@NonNull final ProjectCardViewHolder viewHolder, @NonNull final Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}