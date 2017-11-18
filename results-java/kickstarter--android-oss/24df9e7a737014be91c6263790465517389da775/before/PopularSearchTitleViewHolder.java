package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class PopularSearchTitleViewHolder extends KSViewHolder {

  @Bind(R.id.heading) TextView termTextView;
  @BindString(R.string.search_most_popular) String mostPopularString;

  public PopularSearchTitleViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
    termTextView.setText(mostPopularString);
  }
  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    // no data to bind, this ViewHolder is just a static title
  }
}
