/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.Group;
import im.actor.core.api.PeerType;
import im.actor.core.api.User;
import im.actor.core.api.rpc.ResponseLoadDialogs;
import im.actor.core.api.updates.UpdateChatClear;
import im.actor.core.api.updates.UpdateChatDelete;
import im.actor.core.api.updates.UpdateContactRegistered;
import im.actor.core.api.updates.UpdateContactsAdded;
import im.actor.core.api.updates.UpdateContactsRemoved;
import im.actor.core.api.updates.UpdateCountersChanged;
import im.actor.core.api.updates.UpdateGroupAvatarChanged;
import im.actor.core.api.updates.UpdateGroupInvite;
import im.actor.core.api.updates.UpdateGroupMembersUpdate;
import im.actor.core.api.updates.UpdateGroupOnline;
import im.actor.core.api.updates.UpdateGroupTitleChanged;
import im.actor.core.api.updates.UpdateGroupUserInvited;
import im.actor.core.api.updates.UpdateGroupUserKick;
import im.actor.core.api.updates.UpdateGroupUserLeave;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.api.updates.UpdateMessageContentChanged;
import im.actor.core.api.updates.UpdateMessageDelete;
import im.actor.core.api.updates.UpdateMessageRead;
import im.actor.core.api.updates.UpdateMessageReadByMe;
import im.actor.core.api.updates.UpdateMessageReceived;
import im.actor.core.api.updates.UpdateMessageSent;
import im.actor.core.api.updates.UpdateParameterChanged;
import im.actor.core.api.updates.UpdateTyping;
import im.actor.core.api.updates.UpdateUserAboutChanged;
import im.actor.core.api.updates.UpdateUserAvatarChanged;
import im.actor.core.api.updates.UpdateUserLastSeen;
import im.actor.core.api.updates.UpdateUserLocalNameChanged;
import im.actor.core.api.updates.UpdateUserNameChanged;
import im.actor.core.api.updates.UpdateUserNickChanged;
import im.actor.core.api.updates.UpdateUserOffline;
import im.actor.core.api.updates.UpdateUserOnline;
import im.actor.core.modules.BaseModule;
import im.actor.core.modules.Modules;
import im.actor.core.modules.contacts.ContactsSyncActor;
import im.actor.core.modules.updates.internal.ContactsLoaded;
import im.actor.core.modules.updates.internal.DialogHistoryLoaded;
import im.actor.core.modules.updates.internal.GroupCreated;
import im.actor.core.modules.updates.internal.InternalUpdate;
import im.actor.core.modules.updates.internal.LoggedIn;
import im.actor.core.modules.updates.internal.MessagesHistoryLoaded;
import im.actor.core.modules.updates.internal.UsersFounded;
import im.actor.core.network.parser.Update;
import im.actor.core.viewmodel.UserVM;

public class UpdateProcessor extends BaseModule {

    private static final String TAG = "Updates";

    private SettingsProcessor settingsProcessor;
    private UsersProcessor usersProcessor;
    private MessagesProcessor messagesProcessor;
    private GroupsProcessor groupsProcessor;
    private PresenceProcessor presenceProcessor;
    private TypingProcessor typingProcessor;
    private ContactsProcessor contactsProcessor;

    public UpdateProcessor(Modules modules) {
        super(modules);
        this.settingsProcessor = new SettingsProcessor(modules);
        this.usersProcessor = new UsersProcessor(modules);
        this.messagesProcessor = new MessagesProcessor(modules);
        this.groupsProcessor = new GroupsProcessor(modules);
        this.presenceProcessor = new PresenceProcessor(modules);
        this.typingProcessor = new TypingProcessor(modules);
        this.contactsProcessor = new ContactsProcessor(modules);
    }

    public void applyRelated(List<User> users,
                             List<Group> groups,
                             boolean force) {
        usersProcessor.applyUsers(users, force);
        groupsProcessor.applyGroups(groups, force);
    }

    public void processInternalUpdate(InternalUpdate update) {
        if (update instanceof DialogHistoryLoaded) {
            ResponseLoadDialogs dialogs = ((DialogHistoryLoaded) update).getDialogs();
            applyRelated(dialogs.getUsers(), dialogs.getGroups(), false);
            messagesProcessor.onDialogsLoaded(dialogs);
        } else if (update instanceof MessagesHistoryLoaded) {
            MessagesHistoryLoaded historyLoaded = (MessagesHistoryLoaded) update;
            applyRelated(historyLoaded.getLoadHistory().getUsers(), new ArrayList<Group>(), false);
            messagesProcessor.onMessagesLoaded(historyLoaded.getPeer(), historyLoaded.getLoadHistory());
        } else if (update instanceof LoggedIn) {
            ArrayList<User> users = new ArrayList<User>();
            users.add(((LoggedIn) update).getAuth().getUser());
            applyRelated(users, new ArrayList<Group>(), true);
            runOnUiThread(((LoggedIn) update).getRunnable());
        } else if (update instanceof ContactsLoaded) {
            ContactsLoaded contactsLoaded = (ContactsLoaded) update;
            applyRelated(contactsLoaded.getContacts().getUsers(), new ArrayList<Group>(), false);
            modules().getContactsModule().getContactSyncActor()
                    .send(new ContactsSyncActor.ContactsLoaded(contactsLoaded.getContacts()));
        } else if (update instanceof UsersFounded) {
            final UsersFounded founded = (UsersFounded) update;
            applyRelated(((UsersFounded) update).getUsers(), new ArrayList<Group>(), false);
            final ArrayList<UserVM> users = new ArrayList<UserVM>();
            for (User u : founded.getUsers()) {
                users.add(modules().getUsersModule().getUsersCollection().get(u.getId()));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    founded.getCommandCallback().onResult(users.toArray(new UserVM[users.size()]));
                }
            });
        } else if (update instanceof GroupCreated) {
            final GroupCreated created = (GroupCreated) update;
            ArrayList<Group> groups = new ArrayList<Group>();
            groups.add(created.getGroup());
            applyRelated(created.getUsers(), groups, false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    created.getCallback().onResult(created.getGroup().getId());
                }
            });
        }
    }

    public void applyDifferenceUpdate(List<User> users, List<Group> groups, List<Update> updates) {
        applyRelated(users, groups, false);

        modules().getNotifications().pauseNotifications();
        for (int i = 0; i < updates.size(); i++) {
            processUpdate(updates.get(i));
        }
        modules().getNotifications().resumeNotifications();

        applyRelated(users, groups, true);
    }

    public void processWeakUpdate(Update update, long date) {
        if (update instanceof UpdateUserOnline) {
            UpdateUserOnline userOnline = (UpdateUserOnline) update;
            presenceProcessor.onUserOnline(userOnline.getUid(), date);
        } else if (update instanceof UpdateUserOffline) {
            UpdateUserOffline offline = (UpdateUserOffline) update;
            presenceProcessor.onUserOffline(offline.getUid(), date);
        } else if (update instanceof UpdateUserLastSeen) {
            UpdateUserLastSeen lastSeen = (UpdateUserLastSeen) update;
            presenceProcessor.onUserLastSeen(lastSeen.getUid(), lastSeen.getDate(), date);
        } else if (update instanceof UpdateGroupOnline) {
            UpdateGroupOnline groupOnline = (UpdateGroupOnline) update;
            presenceProcessor.onGroupOnline(groupOnline.getGroupId(), groupOnline.getCount(), date);
        } else if (update instanceof UpdateTyping) {
            UpdateTyping typing = (UpdateTyping) update;
            typingProcessor.onTyping(typing.getPeer(), typing.getUid(), typing.getTypingType());
        }
    }

    public void processUpdate(Update update) {
        // Log.d(TAG, update + "");
        if (update instanceof UpdateUserNameChanged) {
            UpdateUserNameChanged userNameChanged = (UpdateUserNameChanged) update;
            usersProcessor.onUserNameChanged(userNameChanged.getUid(), userNameChanged.getName());
        } else if (update instanceof UpdateUserLocalNameChanged) {
            UpdateUserLocalNameChanged localNameChanged = (UpdateUserLocalNameChanged) update;
            usersProcessor.onUserLocalNameChanged(localNameChanged.getUid(), localNameChanged.getLocalName());
        } else if (update instanceof UpdateUserNickChanged) {
            UpdateUserNickChanged nickChanged = (UpdateUserNickChanged) update;
            usersProcessor.onUserNickChanged(nickChanged.getUid(), nickChanged.getNickname());
        } else if (update instanceof UpdateUserAboutChanged) {
            UpdateUserAboutChanged userAboutChanged = (UpdateUserAboutChanged) update;
            usersProcessor.onUserAboutChanged(userAboutChanged.getUid(), userAboutChanged.getAbout());
        } else if (update instanceof UpdateUserAvatarChanged) {
            UpdateUserAvatarChanged avatarChanged = (UpdateUserAvatarChanged) update;
            usersProcessor.onUserAvatarChanged(avatarChanged.getUid(), avatarChanged.getAvatar());
        } else if (update instanceof UpdateMessage) {
            UpdateMessage message = (UpdateMessage) update;
            messagesProcessor.onMessage(message.getPeer(), message.getSenderUid(), message.getDate(), message.getRid(),
                    message.getMessage());
            typingProcessor.onMessage(message.getPeer(), message.getSenderUid());
        } else if (update instanceof UpdateMessageRead) {
            UpdateMessageRead messageRead = (UpdateMessageRead) update;
            messagesProcessor.onMessageRead(messageRead.getPeer(), messageRead.getStartDate(), messageRead.getReadDate());
        } else if (update instanceof UpdateMessageReadByMe) {
            UpdateMessageReadByMe messageReadByMe = (UpdateMessageReadByMe) update;
            messagesProcessor.onMessageReadByMe(messageReadByMe.getPeer(), messageReadByMe.getStartDate());
        } else if (update instanceof UpdateMessageReceived) {
            UpdateMessageReceived received = (UpdateMessageReceived) update;
            messagesProcessor.onMessageReceived(received.getPeer(), received.getStartDate(), received.getReceivedDate());
        } else if (update instanceof UpdateMessageDelete) {
            UpdateMessageDelete messageDelete = (UpdateMessageDelete) update;
            messagesProcessor.onMessageDelete(messageDelete.getPeer(), messageDelete.getRids());
        } else if (update instanceof UpdateMessageSent) {
            UpdateMessageSent messageSent = (UpdateMessageSent) update;
            messagesProcessor.onMessageSent(messageSent.getPeer(), messageSent.getRid(), messageSent.getDate());
        } else if (update instanceof UpdateMessageContentChanged) {
            UpdateMessageContentChanged contentChanged = (UpdateMessageContentChanged) update;
            messagesProcessor.onMessageContentChanged(contentChanged.getPeer(),
                    contentChanged.getRid(), contentChanged.getMessage());
        } else if (update instanceof UpdateChatClear) {
            UpdateChatClear chatClear = (UpdateChatClear) update;
            messagesProcessor.onChatClear(chatClear.getPeer());
        } else if (update instanceof UpdateChatDelete) {
            UpdateChatDelete chatDelete = (UpdateChatDelete) update;
            messagesProcessor.onChatDelete(chatDelete.getPeer());
        } else if (update instanceof UpdateContactRegistered) {
            UpdateContactRegistered registered = (UpdateContactRegistered) update;
            if (!registered.isSilent()) {
                messagesProcessor.onUserRegistered(registered.getRid(), registered.getUid(), registered.getDate());
            }
        } else if (update instanceof UpdateGroupTitleChanged) {
            UpdateGroupTitleChanged titleChanged = (UpdateGroupTitleChanged) update;
            groupsProcessor.onTitleChanged(titleChanged.getGroupId(), titleChanged.getRid(),
                    titleChanged.getUid(), titleChanged.getTitle(), titleChanged.getDate(),
                    false);
        } else if (update instanceof UpdateGroupAvatarChanged) {
            UpdateGroupAvatarChanged avatarChanged = (UpdateGroupAvatarChanged) update;
            groupsProcessor.onAvatarChanged(avatarChanged.getGroupId(), avatarChanged.getRid(),
                    avatarChanged.getUid(), avatarChanged.getAvatar(),
                    avatarChanged.getDate(), false);
        } else if (update instanceof UpdateGroupInvite) {
            UpdateGroupInvite groupInvite = (UpdateGroupInvite) update;
            groupsProcessor.onGroupInvite(groupInvite.getGroupId(),
                    groupInvite.getRid(), groupInvite.getInviteUid(), groupInvite.getDate(),
                    false);
        } else if (update instanceof UpdateGroupUserLeave) {
            UpdateGroupUserLeave leave = (UpdateGroupUserLeave) update;
            groupsProcessor.onUserLeave(leave.getGroupId(), leave.getRid(), leave.getUid(),
                    leave.getDate(), false);
        } else if (update instanceof UpdateGroupUserKick) {
            UpdateGroupUserKick userKick = (UpdateGroupUserKick) update;
            groupsProcessor.onUserKicked(userKick.getGroupId(),
                    userKick.getRid(), userKick.getUid(), userKick.getKickerUid(), userKick.getDate(),
                    false);
        } else if (update instanceof UpdateGroupUserInvited) {
            UpdateGroupUserInvited userInvited = (UpdateGroupUserInvited) update;
            groupsProcessor.onUserAdded(userInvited.getGroupId(),
                    userInvited.getRid(), userInvited.getUid(), userInvited.getInviterUid(), userInvited.getDate(),
                    false);
        } else if (update instanceof UpdateContactsAdded) {
            UpdateContactsAdded contactsAdded = (UpdateContactsAdded) update;
            int[] res = new int[contactsAdded.getUids().size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = contactsAdded.getUids().get(i);
            }
            contactsProcessor.onContactsAdded(res);
        } else if (update instanceof UpdateContactsRemoved) {
            UpdateContactsRemoved contactsRemoved = (UpdateContactsRemoved) update;
            int[] res = new int[contactsRemoved.getUids().size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = contactsRemoved.getUids().get(i);
            }
            contactsProcessor.onContactsRemoved(res);
        } else if (update instanceof UpdateGroupMembersUpdate) {
            groupsProcessor.onMembersUpdated(((UpdateGroupMembersUpdate) update).getGroupId(),
                    ((UpdateGroupMembersUpdate) update).getMembers());
        } else if (update instanceof UpdateParameterChanged) {
            settingsProcessor.onSettingsChanged(
                    ((UpdateParameterChanged) update).getKey(),
                    ((UpdateParameterChanged) update).getValue());
        } else if (update instanceof UpdateCountersChanged) {
            messagesProcessor.onCountersChanged(((UpdateCountersChanged) update).getCounters());
        }
    }


    public boolean isCausesInvalidation(Update update) {
        HashSet<Integer> users = new HashSet<Integer>();
        HashSet<Integer> groups = new HashSet<Integer>();

        if (update instanceof UpdateMessage) {
            UpdateMessage updateMessage = (UpdateMessage) update;
            users.add(updateMessage.getSenderUid());
            if (updateMessage.getPeer().getType() == PeerType.GROUP) {
                groups.add(updateMessage.getPeer().getId());
            }
            if (updateMessage.getPeer().getType() == PeerType.PRIVATE) {
                users.add(updateMessage.getPeer().getId());
            }
        } else if (update instanceof UpdateContactRegistered) {
            UpdateContactRegistered contactRegistered = (UpdateContactRegistered) update;
            users.add(contactRegistered.getUid());
        } else if (update instanceof UpdateGroupInvite) {
            UpdateGroupInvite groupInvite = (UpdateGroupInvite) update;
            users.add(groupInvite.getInviteUid());
            groups.add(groupInvite.getGroupId());
        } else if (update instanceof UpdateGroupUserInvited) {
            UpdateGroupUserInvited invited = (UpdateGroupUserInvited) update;
            users.add(invited.getInviterUid());
            users.add(invited.getUid());
            groups.add(invited.getGroupId());
        } else if (update instanceof UpdateGroupUserKick) {
            UpdateGroupUserKick kick = (UpdateGroupUserKick) update;
            users.add(kick.getKickerUid());
            users.add(kick.getUid());
            groups.add(kick.getGroupId());
        } else if (update instanceof UpdateGroupUserLeave) {
            UpdateGroupUserLeave leave = (UpdateGroupUserLeave) update;
            users.add(leave.getUid());
            groups.add(leave.getGroupId());
        } else if (update instanceof UpdateContactsAdded) {
            users.addAll(((UpdateContactsAdded) update).getUids());
        } else if (update instanceof UpdateContactsRemoved) {
            users.addAll(((UpdateContactsRemoved) update).getUids());
        } else if (update instanceof UpdateUserLocalNameChanged) {
            UpdateUserLocalNameChanged localNameChanged = (UpdateUserLocalNameChanged) update;
            users.add(localNameChanged.getUid());
        }

        if (!usersProcessor.hasUsers(users)) {
            return true;
        }

        if (!groupsProcessor.hasGroups(groups)) {
            return true;
        }

        return false;
    }
}