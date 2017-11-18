package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.CreatorDashboardAdapter;
import com.kickstarter.viewmodels.CreatorDashboardViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(CreatorDashboardViewModel.ViewModel.class)
public final class CreatorDashboardActivity extends BaseActivity<CreatorDashboardViewModel.ViewModel> {
  private CreatorDashboardAdapter adapter;

  protected @Bind(R.id.creator_dashboard_recycler_view) RecyclerView creatorDashboardRecyclerView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.creator_dashboard_layout);
    ButterKnife.bind(this);

    this.adapter = new CreatorDashboardAdapter();
    this.creatorDashboardRecyclerView.setAdapter(this.adapter);
    this.creatorDashboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.viewModel.outputs.projectAndStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::renderProjectAndStats);

    this.viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(projectAndRefTag -> this.startProjectActivity(projectAndRefTag.first, projectAndRefTag.second));
  }

  private void startProjectActivity(final @NonNull Project project, final @NonNull RefTag refTag) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.REF_TAG, refTag);
    startActivity(intent);
  }

  private void renderProjectAndStats(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndStats) {
    this.adapter.takeProjectAndStats(projectAndStats);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.creatorDashboardRecyclerView.setAdapter(null);
  }
}