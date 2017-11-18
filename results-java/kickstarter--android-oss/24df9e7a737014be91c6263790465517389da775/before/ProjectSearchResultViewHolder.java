package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.viewmodels.ProjectSearchResultHolderViewModel;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

public class ProjectSearchResultViewHolder extends KSViewHolder {
  private final ProjectSearchResultHolderViewModel.ViewModel viewModel;

  protected final Delegate delegate;
  private final KSString ksString;

  @Bind(R.id.project_name_text_view) TextView projectNameTextView;
  @Bind(R.id.project_image_view) ImageView projectImageView;
  @Bind(R.id.project_stats_text_view_percent_complete_data) TextView projectStatsPctCompleteDataTextView;
  @Bind(R.id.project_stats_text_view_percent_complete_string) TextView projectStatsPctCompleteStringTextView;
  @Bind(R.id.project_stats_text_view_days_to_go_data) TextView projectStatsToGoDataTextView;
  @Bind(R.id.project_stats_text_view_days_to_go_string) TextView projectStatsToGoStringTextView;

  @BindString(R.string.discovery_baseball_card_stats_funded) String fundedString;
  @BindString(R.string.discovery_baseball_card_time_left_to_go) String toGoString;

  public interface Delegate {
    void projectSearchResultClick(ProjectSearchResultViewHolder viewHolder, Project project);
  }

  public ProjectSearchResultViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);

    this.viewModel = new ProjectSearchResultHolderViewModel.ViewModel(environment());
    this.delegate = delegate;
    this.ksString = environment().ksString();

    ButterKnife.bind(this, view);

    this.viewModel.outputs.notifyDelegateOfResultClick()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(project -> this.delegate.projectSearchResultClick(this, project));

    this.viewModel.outputs.projectImage()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectImage);

    this.viewModel.outputs.projectName()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.projectNameTextView::setText);

    this.viewModel.outputs.projectStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectStats);

    projectStatsPctCompleteStringTextView.setText(String.format(" %s  ", fundedString));
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final ProjectSearchResultHolderViewModel.Data configData = ObjectUtils.requireNonNull(
      (ProjectSearchResultHolderViewModel.Data) data
    );
    this.viewModel.inputs.configureWith(configData);
  }

  private void setProjectImage(@NonNull final String imageUrl) {
    this.projectImageView.setVisibility(imageUrl == null ? View.INVISIBLE : View.VISIBLE);
    Picasso.with(context()).load(imageUrl).into(projectImageView);
  }

  private void setProjectStats(@NonNull final Pair<Integer, Integer> stats) {
    final int daysToGo = stats.second;
    this.projectStatsToGoDataTextView.setText(String.valueOf(daysToGo));
    this.projectStatsPctCompleteDataTextView.setText(String.valueOf(stats.first+"%"));
    this.projectStatsToGoStringTextView.setText(
      String.format(" %s%s ",
        ksString.format("days", daysToGo),
        ksString.format(toGoString, "time_left", ""))
    );
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.viewModel.inputs.projectClicked();
  }
}
