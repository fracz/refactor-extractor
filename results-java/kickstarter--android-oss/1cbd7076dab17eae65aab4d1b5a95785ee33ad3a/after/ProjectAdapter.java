package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectViewHolder;
import com.kickstarter.ui.viewholders.RewardViewHolder;

import java.util.Collections;
import java.util.List;

import rx.Observable;

public final class ProjectAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectViewHolder.Delegate {}

  public ProjectAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_main_layout;
    } else {
      return R.layout.reward_view;
    }
  }

  /**
   * Populate adapter data when we know we're working with a Project object.
   */
  public void takeProject(final @NonNull Project project, final @NonNull String configCountry) {
    sections().clear();
    sections().add(Collections.singletonList(Pair.create(project, configCountry)));

    final List<Reward> rewards = project.rewards();
    if (rewards != null) {
      addSection(Observable.from(rewards)
        .filter(Reward::isReward)
        .map(reward -> Pair.create(project, reward))
        .toList().toBlocking().single()
      );
    }
    notifyDataSetChanged();
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    if (layout == R.layout.project_main_layout) {
      return new ProjectViewHolder(view, delegate);
    }
    return new RewardViewHolder(view);
  }
}