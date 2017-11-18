package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

public class SearchViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testPopularProjectsLoadImmediately() {
    final SearchViewModel.ViewModel viewModel = new SearchViewModel.ViewModel(environment());

    final TestSubscriber<Boolean> popularProjectsPresent = new TestSubscriber<>();
    viewModel.outputs.popularProjects().map(ps -> ps.size() > 0).subscribe(popularProjectsPresent);

    final TestSubscriber<Boolean> searchProjectsPresent = new TestSubscriber<>();
    viewModel.outputs.searchProjects().map(ps -> ps.size() > 0).subscribe(searchProjectsPresent);

    popularProjectsPresent.assertValues(true);
    searchProjectsPresent.assertNoValues();
  }

  @Test
  public void testSearchProjectsWhenEnterSearchTerm() {
    final TestScheduler scheduler = new TestScheduler();
    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .build();

    final SearchViewModel.ViewModel viewModel = new SearchViewModel.ViewModel(env);

    final TestSubscriber<Boolean> searchProjectsPresent = new TestSubscriber<>();
    viewModel.outputs.searchProjects().map(ps -> ps.size() > 0).subscribe(searchProjectsPresent);

    final TestSubscriber<Boolean> popularProjectsPresent = new TestSubscriber<>();
    viewModel.outputs.popularProjects().map(ps -> ps.size() > 0).subscribe(popularProjectsPresent);

    // Popular projects emit immediately.
    popularProjectsPresent.assertValues(true);
    searchProjectsPresent.assertNoValues();
    koalaTest.assertValues("Discover Search");

    // Searching shouldn't emit values immediately
    viewModel.inputs.search("hello");
    searchProjectsPresent.assertNoValues();
    koalaTest.assertValues("Discover Search");

    // Waiting a small amount time shouldn't emit values
    scheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS);
    searchProjectsPresent.assertNoValues();
    koalaTest.assertValues("Discover Search");

    // Waiting the rest of the time makes the search happen
    scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
    searchProjectsPresent.assertValues(false, true);
    koalaTest.assertValues("Discover Search", "Discover Search Results");

    // Typing more search terms doesn't emit more values
    viewModel.inputs.search("hello world!");
    searchProjectsPresent.assertValues(false, true);
    koalaTest.assertValues("Discover Search", "Discover Search Results");

    // Waiting enough time emits search results
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    searchProjectsPresent.assertValues(false, true, false, true);
    koalaTest.assertValues("Discover Search", "Discover Search Results", "Discover Search Results");

    // Clearing search terms brings back popular projects.
    viewModel.inputs.search("");
    searchProjectsPresent.assertValues(false, true, false, true, false);
    popularProjectsPresent.assertValues(true, false, true);
    koalaTest.assertValues("Discover Search", "Discover Search Results", "Discover Search Results");
  }
}