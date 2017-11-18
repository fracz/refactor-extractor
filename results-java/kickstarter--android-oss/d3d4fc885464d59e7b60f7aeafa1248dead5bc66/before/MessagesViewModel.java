package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Either;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.IntegerUtils;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.BackingActivity;
import com.kickstarter.ui.activities.MessagesActivity;
import com.kickstarter.ui.data.MessageSubject;
import com.kickstarter.ui.data.MessagesData;

import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.errors;
import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.values;

public interface MessagesViewModel {

  interface Inputs {
    /** Call with the app bar's vertical offset value. */
    void appBarOffset(final int verticalOffset);

    /** Call with the app bar's total scroll range. */
    void appBarTotalScrollRange(final int totalScrollRange);

    /** Call when the back or close button has been clicked. */
    void backOrCloseButtonClicked();

    /** Call when the message edit text changes. */
    void messageEditTextChanged(String messageBody);

    /** Call when the message edit text is in focus. */
    void messageEditTextIsFocused(boolean isFocused);

    /** Call when the send message button has been clicked. */
    void sendMessageButtonClicked();

    /** Call when the view pledge button is clicked. */
    void viewPledgeButtonClicked();
  }

  interface Outputs {
    /** Emits a boolean that determines if the back button should be gone. */
    Observable<Boolean> backButtonIsGone();

    /** Emits the backing and project to populate the backing info header. */
    Observable<Pair<Backing, Project>> backingAndProject();

    /** Emits a boolean that determines if the backing info view should be gone. */
    Observable<Boolean> backingInfoViewIsGone();

    /** Emits a boolean that determines if the close button should be gone. */
    Observable<Boolean> closeButtonIsGone();

    /** Emits when we should navigate back. */
    Observable<Void> goBack();

    /** Emits a boolean to determine if the loading indicator should be gone. */
    Observable<Boolean> loadingIndicatorViewIsGone();

    /** Emits a string to display as the message edit text hint. */
    Observable<String> messageEditTextHint();

    /** Emits when the edit text should request focus. */
    Observable<Void> messageEditTextShouldRequestFocus();

    /** Emits a list of messages to be displayed. */
    Observable<List<Message>> messages();

    /** Emits the participant name to be displayed. */
    Observable<String> participantNameTextViewText();

    /** Emits the project name to be displayed. */
    Observable<String> projectNameTextViewText();

    /** Emits the project name to be displayed in the toolbar. */
    Observable<String> projectNameToolbarTextViewText();

    /** Emits the bottom padding for the recycler view. */
    Observable<Void> recyclerViewDefaultBottomPadding();

    /** Emits the initial bottom padding for the recycler view to account for the app bar scroll range. */
    Observable<Integer> recyclerViewInitialBottomPadding();

    /** Emits when the RecyclerView should be scrolled to the bottom. */
    Observable<Void> scrollRecyclerViewToBottom();

    /** Emits a boolean that determines if the Send button should be enabled. */
    Observable<Boolean> sendMessageButtonIsEnabled();

    /** Emits a string to set the message edit text to. */
    Observable<String> setMessageEditText();

    /** Emits a string to display in the message error toast. */
    Observable<String> showMessageErrorToast();

    /** Emits when we should start the {@link BackingActivity}. */
    Observable<Project> startBackingActivity();

    /** Emits when the thread has been marked as read. */
    Observable<Void> successfullyMarkedAsRead();

    /** Emits a boolean to determine when the toolbar should be expanded. */
    Observable<Boolean> toolbarIsExpanded();

    /** Emits a boolean that determines if the View pledge button should be gone. */
    Observable<Boolean> viewPledgeButtonIsGone();
  }

  final class ViewModel extends ActivityViewModel<MessagesActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      final Observable<Either<MessageThread, Pair<Project, Backing>>> configData = intent()
        .map(i -> {
          final MessageThread messageThread = i.getParcelableExtra(IntentKey.MESSAGE_THREAD);
          return messageThread != null
            ? new Either.Left<MessageThread, Pair<Project, Backing>>(messageThread)
            : new Either.Right<MessageThread, Pair<Project, Backing>>(
              Pair.create(i.getParcelableExtra(IntentKey.PROJECT), i.getParcelableExtra(IntentKey.BACKING))
          );
        });

      final Observable<KoalaContext.Message> koalaContext = intent()
        .map(i -> i.getSerializableExtra(IntentKey.KOALA_CONTEXT))
        .ofType(KoalaContext.Message.class);

      final Observable<Backing> configBacking = configData
        .map(Either::right)
        .filter(ObjectUtils::isNotNull)
        .map(PairUtils::second);

      final Observable<MessageThread> configThread = configData
        .map(Either::left)
        .filter(ObjectUtils::isNotNull);

      final Observable<Either<Backing, MessageThread>> backingOrThread = Observable.merge(
        configBacking.map(backing -> new Either.Left<>(backing)),
        configThread.map(thread -> new Either.Right<>(thread))
      );

      final PublishSubject<Boolean> messageIsSending = PublishSubject.create();
      final PublishSubject<Boolean> messagesAreLoading = PublishSubject.create();

      final Observable<Project> project = configData
        .map(data -> data.either(MessageThread::project, projectAndBacking -> projectAndBacking.first));

      final Observable<MessageThreadEnvelope> initialMessageThreadEnvelope = backingOrThread
        .switchMap(bOrT -> {
          final Observable<MessageThreadEnvelope> response = bOrT.either(
            this.client::fetchMessagesForBacking,
            this.client::fetchMessagesForThread
          );

          return response
            .doOnSubscribe(() -> messagesAreLoading.onNext(true))
            .doAfterTerminate(() -> messagesAreLoading.onNext(false))
            .compose(neverError())
            .share();
        });

      this.loadingIndicatorViewIsGone = messagesAreLoading
        .map(BooleanUtils::negate)
        .distinctUntilChanged();

      // If view model was not initialized with a MessageThread, participant is
      // the project creator.
      final Observable<User> participant = Observable.merge(
        initialMessageThreadEnvelope
          .map(MessageThreadEnvelope::messageThread)
          .filter(ObjectUtils::isNotNull)
          .map(MessageThread::participant),
        project.map(Project::creator)
      )
        .take(1);

      final Observable<MessagesData> messagesData = Observable.combineLatest(
        backingOrThread,
        project,
        participant,
        this.currentUser.observable(),
        MessagesData::new
      );

      final Observable<MessageSubject> messageSubject = messagesData
        .map(data ->
          data.getBackingOrThread().either(
            // Message subject is the project if the current user is the backer,
            // otherwise the current user is the creator and will send a message to the backing.
            backing -> backing.backerId() == data.getCurrentUser().id()
              ? new MessageSubject.Project(data.getProject())
              : new MessageSubject.Backing(backing),
            // If instantiated with a message thread the thread is the subject.
            MessageSubject.MessageThread::new
          )
        );

      final Observable<Notification<Message>> messageNotification = messageSubject
        .compose(combineLatestPair(this.messageEditTextChanged))
        .compose(takeWhen(this.sendMessageButtonClicked))
        .switchMap(messageSubjectAndBody ->
          this.client.sendMessage(messageSubjectAndBody.first, messageSubjectAndBody.second)
            .doOnSubscribe(() -> messageIsSending.onNext(true))
        )
        .materialize()
        .share();

      final Observable<Message> messageSent = messageNotification.compose(values()).ofType(Message.class);

      final Observable<MessageThreadEnvelope> sentMessageThreadEnvelope = backingOrThread
        .compose(takeWhen(messageSent))
        .switchMap(bOrT -> bOrT.either(this.client::fetchMessagesForBacking, this.client::fetchMessagesForThread))
        .compose(neverError())
        .share();

      final Observable<MessageThreadEnvelope> messageThreadEnvelope = Observable.merge(
        initialMessageThreadEnvelope,
        sentMessageThreadEnvelope
      )
        .distinctUntilChanged();

      final Observable<Boolean> messageHasBody = this.messageEditTextChanged
        .map(StringUtils::isPresent);

      messageThreadEnvelope
        .map(MessageThreadEnvelope::messageThread)
        .filter(ObjectUtils::isNotNull)
        .switchMap(this.client::markAsRead)
        .materialize()
        .compose(ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(this.successfullyMarkedAsRead::onNext);

      final Observable<List<Message>> initialMessages = initialMessageThreadEnvelope
        .map(MessageThreadEnvelope::messages);

      final Observable<List<Message>> newMessages = sentMessageThreadEnvelope
        .map(MessageThreadEnvelope::messages);

      // Concat distinct messages to initial message list. Return just the new messages if
      // initial list is null, i.e. a new message thread.
      final Observable<List<Message>> updatedMessages = initialMessages
        .compose(takePairWhen(newMessages))
        .map(mm -> mm.first == null ? mm.second : ListUtils.concatDistinct(mm.first, mm.second));

      // Load the initial messages once, subsequently load newer messages if any.
      initialMessages
        .filter(ObjectUtils::isNotNull)
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(this.messages::onNext);

      updatedMessages
        .compose(bindToLifecycle())
        .subscribe(this.messages::onNext);

      participant
        .map(User::name)
        .compose(bindToLifecycle())
        .subscribe(this.participantNameTextViewText::onNext);

      initialMessageThreadEnvelope
        .map(MessageThreadEnvelope::messages)
        .filter(ObjectUtils::isNull)
        .take(1)
        .compose(ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(this.messageEditTextShouldRequestFocus::onNext);

      messagesData
        .switchMap(data -> backingAndProjectFromData(data, this.client))
        .filter(ObjectUtils::isNotNull)
        .compose(bindToLifecycle())
        .subscribe(this.backingAndProject::onNext);

      messagesData
        .switchMap(data -> backingAndProjectFromData(data, this.client))
        .map(ObjectUtils::isNull)
        .compose(bindToLifecycle())
        .subscribe(this.backingInfoViewIsGone::onNext);

      koalaContext
        .map(c -> c.equals(KoalaContext.Message.BACKER_MODAL))
        .compose(bindToLifecycle())
        .subscribe(this.viewPledgeButtonIsGone::onNext);

      this.backButtonIsGone = this.viewPledgeButtonIsGone.map(BooleanUtils::negate);
      this.closeButtonIsGone = this.backButtonIsGone.map(BooleanUtils::negate);
      this.goBack = this.backOrCloseButtonClicked;
      this.messageEditTextHint = this.participantNameTextViewText;
      this.projectNameToolbarTextViewText = this.projectNameTextViewText;
      this.scrollRecyclerViewToBottom = updatedMessages.compose(ignoreValues());
      this.sendMessageButtonIsEnabled = Observable.merge(messageHasBody, messageIsSending.map(BooleanUtils::negate));
      this.setMessageEditText = messageSent.map(__ -> "");

      this.toolbarIsExpanded = this.messages
        .compose(takePairWhen(this.messageEditTextIsFocused))
        .map(PairUtils::second)
        .map(BooleanUtils::negate);

      messageNotification
        .compose(errors())
        .map(ErrorEnvelope::fromThrowable)
        .map(ErrorEnvelope::errorMessage)
        .subscribe(this.showMessageErrorToast::onNext);

      project
        .map(Project::name)
        .compose(bindToLifecycle())
        .subscribe(this.projectNameTextViewText::onNext);

      project
        .compose(takeWhen(this.viewPledgeButtonClicked))
        .compose(bindToLifecycle())
        .subscribe(this.startBackingActivity::onNext);

      project
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackViewedMessageThread);

      // Set only the initial padding once to counteract the appbar offset.
      this.recyclerViewInitialBottomPadding = this.appBarTotalScrollRange.take(1);

      // Take only the first instance in which the offset changes.
      this.recyclerViewDefaultBottomPadding = this.appBarOffset
        .filter(IntegerUtils::isNonZero)
        .compose(ignoreValues())
        .take(1);

      Observable.combineLatest(project, koalaContext, Pair::create)
        .compose(takeWhen(messageSent))
        .compose(bindToLifecycle())
        .subscribe(pc -> this.koala.trackSentMessage(pc.first, pc.second));
    }

    private static @Nullable Observable<Pair<Backing, Project>> backingAndProjectFromData(final @NonNull MessagesData data,
      final @NonNull ApiClientType client) {

      return data.getBackingOrThread().either(
        backing -> Observable.just(Pair.create(backing, data.getProject())),
        thread -> {
          final Observable<Notification<Backing>> backingNotification = data.getProject().isBacking()
            ? client.fetchProjectBacking(data.getProject(), data.getCurrentUser()).materialize().share()
            : client.fetchProjectBacking(data.getProject(), data.getParticipant()).materialize().share();

          return Observable.merge(
            backingNotification.compose(errors()).map(__ -> null),
            backingNotification.compose(values()).map(b -> Pair.create(b, data.getProject()))
          )
            .take(1);
        }
      );
    }

    private final PublishSubject<Integer> appBarOffset = PublishSubject.create();
    private final PublishSubject<Integer> appBarTotalScrollRange = PublishSubject.create();
    private final PublishSubject<Void> backOrCloseButtonClicked = PublishSubject.create();
    private final PublishSubject<String> messageEditTextChanged = PublishSubject.create();
    private final PublishSubject<Boolean> messageEditTextIsFocused = PublishSubject.create();
    private final PublishSubject<Void> sendMessageButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> viewPledgeButtonClicked = PublishSubject.create();

    private final Observable<Boolean> backButtonIsGone;
    private final BehaviorSubject<Pair<Backing, Project>> backingAndProject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> backingInfoViewIsGone = BehaviorSubject.create();
    private final Observable<Boolean> closeButtonIsGone;
    private final Observable<Void> goBack;
    private final Observable<Boolean> loadingIndicatorViewIsGone;
    private final Observable<String> messageEditTextHint;
    private final PublishSubject<Void> messageEditTextShouldRequestFocus = PublishSubject.create();
    private final BehaviorSubject<List<Message>> messages = BehaviorSubject.create();
    private final BehaviorSubject<String> participantNameTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<String> projectNameTextViewText = BehaviorSubject.create();
    private final Observable<String> projectNameToolbarTextViewText;
    private final Observable<Void> recyclerViewDefaultBottomPadding;
    private final Observable<Integer> recyclerViewInitialBottomPadding;
    private final Observable<Void> scrollRecyclerViewToBottom;
    private final PublishSubject<String> showMessageErrorToast = PublishSubject.create();
    private final Observable<Boolean> sendMessageButtonIsEnabled;
    private final Observable<String> setMessageEditText;
    private final PublishSubject<Project> startBackingActivity = PublishSubject.create();
    private final BehaviorSubject<Void> successfullyMarkedAsRead = BehaviorSubject.create();
    private final Observable<Boolean> toolbarIsExpanded;
    private final BehaviorSubject<Boolean> viewPledgeButtonIsGone = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void appBarOffset(final int verticalOffset) {
      this.appBarOffset.onNext(verticalOffset);
    }
    @Override public void appBarTotalScrollRange(final int totalScrollRange) {
      this.appBarTotalScrollRange.onNext(totalScrollRange);
    }
    @Override public void backOrCloseButtonClicked() {
      this.backOrCloseButtonClicked.onNext(null);
    }
    @Override public void messageEditTextChanged(final @NonNull String messageBody) {
      this.messageEditTextChanged.onNext(messageBody);
    }
    @Override public void messageEditTextIsFocused(final boolean isFocused) {
      this.messageEditTextIsFocused.onNext(isFocused);
    }
    @Override public void sendMessageButtonClicked() {
      this.sendMessageButtonClicked.onNext(null);
    }
    @Override public void viewPledgeButtonClicked() {
      this.viewPledgeButtonClicked.onNext(null);
    }

    @Override public @NonNull Observable<Boolean> backButtonIsGone() {
      return this.backButtonIsGone;
    }
    @Override public @NonNull Observable<Pair<Backing, Project>> backingAndProject() {
      return this.backingAndProject;
    }
    @Override public @NonNull Observable<Boolean> backingInfoViewIsGone() {
      return this.backingInfoViewIsGone;
    }
    @Override public @NonNull Observable<Boolean> closeButtonIsGone() {
      return this.closeButtonIsGone;
    }
    @Override public @NonNull Observable<Void> goBack() {
      return this.goBack;
    }
    @Override public @NonNull Observable<Boolean> loadingIndicatorViewIsGone() {
      return this.loadingIndicatorViewIsGone;
    }
    @Override public @NonNull Observable<String> messageEditTextHint() {
      return this.messageEditTextHint;
    }
    @Override public @NonNull Observable<Void> messageEditTextShouldRequestFocus() {
      return this.messageEditTextShouldRequestFocus;
    }
    @Override public @NonNull Observable<List<Message>> messages() {
      return this.messages;
    }
    @Override public @NonNull Observable<String> participantNameTextViewText() {
      return this.participantNameTextViewText;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
    @Override public @NonNull Observable<String> projectNameToolbarTextViewText() {
      return this.projectNameToolbarTextViewText;
    }
    @Override public @NonNull Observable<Void> recyclerViewDefaultBottomPadding() {
      return this.recyclerViewDefaultBottomPadding;
    }
    @Override public @NonNull Observable<Integer> recyclerViewInitialBottomPadding() {
      return this.recyclerViewInitialBottomPadding;
    }
    @Override public @NonNull Observable<Void> scrollRecyclerViewToBottom() {
      return this.scrollRecyclerViewToBottom;
    }
    @Override public @NonNull Observable<String> showMessageErrorToast() {
      return this.showMessageErrorToast;
    }
    @Override public @NonNull Observable<Boolean> sendMessageButtonIsEnabled() {
      return this.sendMessageButtonIsEnabled;
    }
    @Override public @NonNull Observable<String> setMessageEditText() {
      return this.setMessageEditText;
    }
    @Override public @NonNull Observable<Project> startBackingActivity() {
      return this.startBackingActivity;
    }
    @Override public @NonNull Observable<Void> successfullyMarkedAsRead() {
      return this.successfullyMarkedAsRead;
    }
    @Override public @NonNull Observable<Boolean> toolbarIsExpanded() {
      return this.toolbarIsExpanded;
    }
    @Override public @NonNull Observable<Boolean> viewPledgeButtonIsGone() {
      return this.viewPledgeButtonIsGone;
    }
  }
}