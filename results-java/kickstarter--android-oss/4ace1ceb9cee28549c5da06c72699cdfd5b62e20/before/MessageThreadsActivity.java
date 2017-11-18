package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.adapters.MessageThreadsAdapter;
import com.kickstarter.viewmodels.MessageThreadsViewModel;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(MessageThreadsViewModel.ViewModel.class)
public class MessageThreadsActivity extends BaseActivity<MessageThreadsViewModel.ViewModel> {
  private MessageThreadsAdapter adapter;
  private KSString ksString;
  private RecyclerViewPaginator recyclerViewPaginator;

  protected @Bind(R.id.message_threads_app_bar_layout) AppBarLayout appBarLayout;
  protected @Bind(R.id.mailbox_text_view) TextView mailboxTextView;
  protected @Bind(R.id.mailbox_and_unread_layout) View expandedToolbarTitle;
  protected @Bind(R.id.message_threads_collapsed_toolbar_title) View collapsedToolbarTitle;
  protected @Bind(R.id.message_threads_collapsing_toolbar_layout) CollapsingToolbarLayout collapsingToolbarLayout;
  protected @Bind(R.id.message_threads_recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.unread_count_text_view) TextView unreadCountTextView;
  protected @Bind(R.id.message_threads_toolbar_unread_count_text_view) TextView unreadCountToolbarTextView;

  protected @BindString(R.string.messages_navigation_inbox) String inboxString;
  protected @BindString(R.string.No_messages) String noMessagesString;
  protected @BindString(R.string.No_unread_messages) String noUnreadMessagesString;
  protected @BindString(R.string.unread_count_unread) String unreadCountUnreadString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.message_threads_layout);
    ButterKnife.bind(this);

    this.adapter = new MessageThreadsAdapter();
    this.ksString = environment().ksString();
    this.recyclerView.setAdapter(this.adapter);
    this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.recyclerViewPaginator = new RecyclerViewPaginator(this.recyclerView, this.viewModel.inputs::nextPage);

    this.mailboxTextView.setText(this.inboxString);  // todo: Sent mailbox logic enum

    setOffsetChangedListener();

    this.viewModel.outputs.hasNoMessages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.unreadCountTextView.setText(this.noMessagesString));

    this.viewModel.outputs.hasNoUnreadMessages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.unreadCountTextView.setText(this.noUnreadMessagesString));

    this.viewModel.outputs.messageThreads()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.adapter::messageThreads);

    this.viewModel.outputs.unreadCountToolbarTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.unreadCountToolbarTextView));

    this.viewModel.outputs.unreadMessagesCount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setUnreadTextViewText);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.recyclerViewPaginator.stop();
    this.recyclerView.setAdapter(null);
  }

  @Override
  protected void onResume() {
    super.onResume();
    this.viewModel.inputs.onResume();
  }

  // todo: make inputs and outputs
  private void setOffsetChangedListener() {
    this.appBarLayout.addOnOffsetChangedListener(
      (appBarLayout, offset) -> {
        if (offset == 0) {  // expanded
          this.expandedToolbarTitle.setVisibility(View.VISIBLE);
          this.collapsedToolbarTitle.setVisibility(View.INVISIBLE);

        } else if (Math.abs(offset) >= appBarLayout.getTotalScrollRange()) {
          this.expandedToolbarTitle.setVisibility(View.INVISIBLE);
          this.collapsedToolbarTitle.setVisibility(View.VISIBLE);
        }
      }
    );
  }

  private void setUnreadTextViewText(final @NonNull Integer unreadCount) {
    final String unreadCountString = NumberUtils.format(unreadCount);
    this.unreadCountTextView.setText(
      this.ksString.format(this.unreadCountUnreadString, "unread_count", unreadCountString)
    );
    this.unreadCountToolbarTextView.setText(StringUtils.wrapInParentheses(unreadCountString));
  }
}