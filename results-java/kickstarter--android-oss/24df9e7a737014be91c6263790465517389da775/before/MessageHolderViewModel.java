package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Message;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.viewholders.MessageViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;

public interface MessageHolderViewModel {

  interface Inputs {
    /** Call to configure the view model with a message. */
    void configureWith(Message message);
  }

  interface Outputs {
    /** Emits a boolean that determines whether the participant's avatar image should be hidden. */
    Observable<Boolean> participantAvatarImageHidden();

    /** Emits the url for the participant's avatar image. */
    Observable<String> participantAvatarImageUrl();

    /** Emits a boolean to determine whether the message body view should align the parent view's end and right. */
    Observable<Boolean> messageBodyTextViewAlignParentEnd();

    /** Emits the color int for the message background. */
    Observable<Integer> messageBodyTextViewBackgroundColorInt();

    /** Emits the message body text view text. */
    Observable<String> messageBodyTextViewText();

    /** Emits the color int for the message text. */
    Observable<Integer> messageBodyTextViewTextColorInt();
  }

  final class ViewModel extends ActivityViewModel<MessageViewHolder> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      final Observable<Pair<Message, Boolean>> messageAndCurrentUserIsSender = this.message
        .compose(combineLatestPair(this.currentUser.loggedInUser()))
        .map(mu -> Pair.create(mu.first, mu.first.sender().id() == mu.second.id()));

      this.participantAvatarImageHidden = messageAndCurrentUserIsSender.map(mb -> mb.second);

      this.participantAvatarImageUrl = messageAndCurrentUserIsSender
        .filter(mb -> !mb.second)
        .map(mb -> mb.first.sender().avatar().medium());

      this.messageBodyTextViewAlignParentEnd = messageAndCurrentUserIsSender.map(mb -> mb.second);

      this.messageBodyTextViewText = this.message.map(Message::body);

      this.messageBodyTextViewBackgroundColor = messageAndCurrentUserIsSender
        .map(mb -> mb.second ? R.color.black : R.color.ksr_grey_400);

      this.messageBodyTextViewTextColor = messageAndCurrentUserIsSender
        .map(mb -> mb.second ? R.color.white : R.color.ksr_navy_700);
    }

    private final PublishSubject<Message> message = PublishSubject.create();

    private final Observable<Boolean> participantAvatarImageHidden;
    private final Observable<String> participantAvatarImageUrl;
    private final Observable<Boolean> messageBodyTextViewAlignParentEnd;
    private final Observable<Integer> messageBodyTextViewBackgroundColor;
    private final Observable<String> messageBodyTextViewText;
    private final Observable<Integer> messageBodyTextViewTextColor;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void configureWith(final @NonNull Message message) {
      this.message.onNext(message);
    }

    @Override public @NonNull Observable<Boolean> participantAvatarImageHidden() {
      return this.participantAvatarImageHidden;
    }
    @Override public @NonNull Observable<String> participantAvatarImageUrl() {
      return this.participantAvatarImageUrl;
    }
    @Override public @NonNull Observable<Boolean> messageBodyTextViewAlignParentEnd() {
      return this.messageBodyTextViewAlignParentEnd;
    }
    @Override public @NonNull Observable<Integer> messageBodyTextViewBackgroundColorInt() {
      return this.messageBodyTextViewBackgroundColor;
    }
    @Override public @NonNull Observable<String> messageBodyTextViewText() {
      return this.messageBodyTextViewText;
    }
    @Override public @NonNull Observable<Integer> messageBodyTextViewTextColorInt() {
      return this.messageBodyTextViewTextColor;
    }
  }
}