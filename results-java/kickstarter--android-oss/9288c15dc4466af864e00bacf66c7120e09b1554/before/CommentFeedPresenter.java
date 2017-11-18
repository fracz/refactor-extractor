package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.widget.Toast;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.ui.activities.CommentFeedActivity;
import com.kickstarter.ui.adapters.CommentFeedAdapter;
import com.kickstarter.ui.viewholders.EmptyCommentFeedViewHolder;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class CommentFeedPresenter extends Presenter<CommentFeedActivity> implements CommentFeedAdapter.Delegate {
  private final PublishSubject<Void> contextClick = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  // todo: add pagination to comments
  public void initialize(@NonNull final Project initialProject) {

    final Observable<Project> project = loginSuccess.flatMap(__ -> client.fetchProject(initialProject))
      .startWith(initialProject);

    final Observable<List<Comment>> comments = client.fetchProjectComments(initialProject)
      .map(CommentsEnvelope::comments);

    final Observable<List<?>> viewCommentsProject = Observable.combineLatest(
      Arrays.asList(viewSubject, comments, project),
      Arrays::asList);

    final Observable<Pair<CommentFeedActivity, Project>> viewAndProject =
      RxUtils.combineLatestPair(viewSubject, Observable.just(initialProject));

    // logged in user == backer --> post comment
    addSubscription(RxUtils.takeWhen(viewSubject, loginSuccess)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(CommentFeedActivity::showCommentDialog)
    );

    addSubscription(RxUtils.takePairWhen(currentUser.observable(), viewCommentsProject)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(uvcp -> {
        final User u = uvcp.first;
        final CommentFeedActivity view = (CommentFeedActivity) uvcp.second.get(0);
        final List<Comment> cs = (List<Comment>) uvcp.second.get(1);
        final Project p = (Project) uvcp.second.get(2);
        view.show(p, cs, u);
      }));

    addSubscription(RxUtils.takeWhen(viewSubject, contextClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(CommentFeedActivity::onBackPressed));

    addSubscription(RxUtils.takeWhen(viewSubject, loginClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(CommentFeedActivity::commentFeedLogin));
  }

  public void emptyCommentFeedLoginClicked(@NonNull final EmptyCommentFeedViewHolder viewHolder) {
    loginClick.onNext(null);
  }

  public void postCommentOnClick(@NonNull final Project project, @NonNull final String body) {
    client.postProjectComment(project, body)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showToastOnSuccess);
  }

  private void showToastOnSuccess(@Nullable final Comment comment) {
    final String message = (comment == null) ? "Comment could not be posted" : "Comment posted!";
    final Toast toast = Toast.makeText(view().getApplicationContext(), message, Toast.LENGTH_LONG);
    toast.show();
  }

  public void projectContextClicked() {
    contextClick.onNext(null);
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
  }
}