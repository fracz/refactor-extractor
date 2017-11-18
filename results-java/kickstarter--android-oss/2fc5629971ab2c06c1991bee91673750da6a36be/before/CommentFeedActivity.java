package com.kickstarter.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.kickstarter.ui.adapters.CommentFeedAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresPresenter(CommentFeedPresenter.class)
public class CommentFeedActivity extends BaseActivity<CommentFeedPresenter> {
  private CommentFeedAdapter adapter;

  @Bind(R.id.comment_button_backing) TextView commentButtonTextView;
  @Bind(R.id.comment_feed_recycler_view) RecyclerView recyclerView; // rename
  @Nullable @Bind(R.id.context_photo) ImageView projectPhotoImageView;
  @Nullable @Bind(R.id.project_name) TextView projectNameTextView;
  @Nullable @Bind(R.id.creator_name) TextView creatorNameTextView;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.comment_feed_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    final Project project = intent.getParcelableExtra(getString(R.string.intent_project));
    presenter.initialize(project);

    adapter = new CommentFeedAdapter(presenter, project);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    if (project.isBacking()) {
      commentButtonTextView.setVisibility(View.VISIBLE);
    }
  }

  public void show(@NonNull final Project project, @Nullable final List<Comment> comments) {
    adapter.takeProjectComments(project, comments);
  }

  @Nullable
  @OnClick({R.id.nav_back_button, R.id.project_context_view})
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void commentFeedLogin() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_forward), true);
    startActivityForResult(intent, ActivityRequestCodes.COMMENT_FEED_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  // This has to stay here because logged in backers can be on either an
  // empty or non-empty comment feed view.
  @Nullable
  @OnClick(R.id.comment_button_backing)
  public void publicCommentClick() {

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