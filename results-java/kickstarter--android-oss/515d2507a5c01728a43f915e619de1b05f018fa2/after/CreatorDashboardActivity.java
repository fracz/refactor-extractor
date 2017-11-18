package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.adapters.CreatorDashboardBottomSheetAdapter;
import com.kickstarter.ui.fragments.CreatorDashboardFragment;
import com.kickstarter.viewmodels.CreatorDashboardViewModel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(CreatorDashboardViewModel.ViewModel.class)
public final class CreatorDashboardActivity extends BaseActivity<CreatorDashboardViewModel.ViewModel> {

  private CreatorDashboardBottomSheetAdapter bottomSheetAdapter;
  private BottomSheetBehavior bottomSheetBehavior;

  protected @Bind(R.id.creator_dashboard_bottom_sheet_recycler_view) RecyclerView bottomSheetRecyclerView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.creator_dashboard_layout);
    ButterKnife.bind(this);

    // Set up the bottom sheet recycler view.
    this.bottomSheetAdapter = new CreatorDashboardBottomSheetAdapter();
    this.bottomSheetRecyclerView.setAdapter(this.bottomSheetAdapter);
    this.bottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // todo: reuse LayoutManager?
    this.bottomSheetBehavior = BottomSheetBehavior.from(this.bottomSheetRecyclerView);

    this.viewModel.outputs.projectAndStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::createProjectDashboardFragments);

    this.viewModel.outputs.projectsForBottomSheet()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectsForDropdown);
  }

  private void setProjectsForDropdown(final @NonNull List<Project> projects) {
    this.bottomSheetAdapter.takeProjects(projects);
  }

  private void createProjectDashboardFragments(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndStats) {
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    final CreatorDashboardFragment fragment = CreatorDashboardFragment.newInstance(projectAndStats);
    fragmentTransaction.add(R.id.creator_dashboard_coordinator_view, fragment);
    fragmentTransaction.commit();
  }

  public void toggleBottomSheetClick() {
    this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    this.bottomSheetRecyclerView.bringToFront();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.bottomSheetRecyclerView.setAdapter(null);
  }
}