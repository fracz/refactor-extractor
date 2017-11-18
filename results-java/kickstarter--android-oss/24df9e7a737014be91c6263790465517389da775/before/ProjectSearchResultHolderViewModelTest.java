package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.PhotoFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;

import org.joda.time.DateTime;
import org.junit.Test;

import rx.observers.TestSubscriber;

public final class ProjectSearchResultHolderViewModelTest extends KSRobolectricTestCase {
  private ProjectSearchResultHolderViewModel.ViewModel vm;
  private final TestSubscriber<Project> notifyDelegateOfResultClick = new TestSubscriber<>();
  private final TestSubscriber<String> projectImage = new TestSubscriber<>();
  private final TestSubscriber<String> projectName = new TestSubscriber<>();
  private final TestSubscriber<Pair<Integer, Integer>> projectStats = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ProjectSearchResultHolderViewModel.ViewModel(environment);

    this.vm.outputs.notifyDelegateOfResultClick().subscribe(this.notifyDelegateOfResultClick);
    this.vm.outputs.projectImage().subscribe(this.projectImage);
    this.vm.outputs.projectName().subscribe(this.projectName);
    this.vm.outputs.projectStats().subscribe(this.projectStats);
  }

  @Test
  public void testEmitsProjectImage() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .photo(
        PhotoFactory.photo()
          .toBuilder()
          .med("http://www.kickstarter.com/med.jpg")
          .build()
      )
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(new ProjectSearchResultHolderViewModel.Data(project, false));

    this.projectImage.assertValues("http://www.kickstarter.com/med.jpg");
  }

  @Test
  public void testEmitsFeaturedProjectImage() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .photo(
        PhotoFactory.photo()
          .toBuilder()
          .full("http://www.kickstarter.com/full.jpg")
          .build()
      )
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(new ProjectSearchResultHolderViewModel.Data(project, true));

    this.projectImage.assertValues("http://www.kickstarter.com/full.jpg");
  }

  @Test
  public void testEmitsProjectName() {
    final Project project = ProjectFactory.project();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(new ProjectSearchResultHolderViewModel.Data(project, true));
    this.projectName.assertValues(project.name());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testEmitsProjectStats() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .pledged(100)
      .goal(200)
      .deadline(new DateTime().plusHours(24 * 10 + 1))
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(new ProjectSearchResultHolderViewModel.Data(project, true));
    this.projectStats.assertValues(new Pair<>(50, 10));
  }

  @Test
  public void testEmitsProjectClicked() {
    final Project project = ProjectFactory.project();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(new ProjectSearchResultHolderViewModel.Data(project, true));
    this.vm.inputs.projectClicked();

    this.notifyDelegateOfResultClick.assertValues(project);
  }
}