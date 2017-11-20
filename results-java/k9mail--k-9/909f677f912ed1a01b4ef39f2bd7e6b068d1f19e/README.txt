commit 909f677f912ed1a01b4ef39f2bd7e6b068d1f19e
Author: Jesse Vincent <jesse@fsck.com>
Date:   Mon Jun 1 01:35:05 2009 +0000

    r62972@17h:  jesse | 2009-05-07 10:49:32 -0400
    First stab at a folderlist that doesn't know or care about messages
    r62973@17h:  jesse | 2009-05-07 10:50:11 -0400
    A very broken first stab at a message list that only knows about one folder.
    r62974@17h:  jesse | 2009-05-07 10:50:44 -0400
    When you go from an account list to an individual account, open a folderlist, not an fml
    r62975@17h:  jesse | 2009-05-07 10:51:24 -0400
    Update Welcome activity to open an ml instead of an fml
    r62976@17h:  jesse | 2009-05-07 10:51:59 -0400
    When setting up accounts is over, open an fl instead of an fml
    r62977@17h:  jesse | 2009-05-07 10:52:51 -0400
    Update MessageView to use folderinfoholders and messageinfoholders from the 'correct' classes.
    r62978@17h:  jesse | 2009-05-07 10:59:07 -0400
    MailService now notifies the fl instead of the fml. Not sure if it should also notify the ml. - will require testing
    r62979@17h:  jesse | 2009-05-07 11:01:09 -0400
    Switch MessagingController's notifications from notifying the FML to notifying an ML
    r62980@17h:  jesse | 2009-05-07 11:25:22 -0400
    Update AndroidManifest to know about the new world order
    r62981@17h:  jesse | 2009-05-07 11:26:11 -0400
    Try to follow the android sdk docs for intent creation
    r62982@17h:  jesse | 2009-05-07 11:28:30 -0400
    reset MessageList for another try at the conversion
    r62983@17h:  jesse | 2009-05-07 11:47:33 -0400
    This version doesn't crash and has a working 'folder' layer. now to clean up the message list layer
    r62984@17h:  jesse | 2009-05-07 15:18:04 -0400
     move step 1
    r62985@17h:  jesse | 2009-05-07 15:18:37 -0400
    move step 1
    r62986@17h:  jesse | 2009-05-07 15:22:47 -0400
    rename step 1
    r62987@17h:  jesse | 2009-05-07 17:38:02 -0400
     checkpoint to move
    r62988@17h:  jesse | 2009-05-07 17:40:01 -0400
    checkpointing a state with a working folder list and a message list that doesn't explode
    r62989@17h:  jesse | 2009-05-07 17:40:26 -0400
    Remove debugging cruft from Welcome
    r62990@17h:  jesse | 2009-05-07 22:00:12 -0400
    Basic functionality works.
    r62991@17h:  jesse | 2009-05-08 04:19:52 -0400
    added a tool to build a K-9 "Beta"
    r62992@17h:  jesse | 2009-05-08 04:20:03 -0400
    remove a disused file
    r62993@17h:  jesse | 2009-05-09 06:07:02 -0400
    upgrading build infrastructure for the 1.5 sdk
    r62994@17h:  jesse | 2009-05-09 06:22:02 -0400
    further refine onOpenMessage, removing more folder assumptions
    r62995@17h:  jesse | 2009-05-09 20:07:20 -0400
    Make the Welcome activity open the autoexpandfolder rather than INBOX
    r62996@17h:  jesse | 2009-05-09 20:14:10 -0400
    MessageList now stores the Folder name it was working with across pause-reload
    r62997@17h:  jesse | 2009-05-09 20:14:26 -0400
    Removing dead code from FolderList
    r63060@17h:  jesse | 2009-05-10 00:07:33 -0400
    Replace the old message list refreshing code which cleared and rebuilt the list from scratch with code which updates or deletes existing messages.
    Add "go back to folder list" code
    r63061@17h:  jesse | 2009-05-10 00:07:50 -0400
    fix message list menus for new world order
    r63062@17h:  jesse | 2009-05-10 00:08:11 -0400
    Remove message list options from folder list menus
    r63063@17h:  jesse | 2009-05-10 00:10:02 -0400
    remove more message list options from the folder list
    r63064@17h:  jesse | 2009-05-10 00:10:19 -0400
    fix build.xml for the new android world order
    r63065@17h:  jesse | 2009-05-10 00:39:23 -0400
    reformatted in advance of bug tracing
    r63066@17h:  jesse | 2009-05-10 05:53:28 -0400
    fix our 'close' behavior to not leave extra activities around
    clean up more vestigal code
    r63067@17h:  jesse | 2009-05-10 18:44:25 -0400
    Improve "back button / accounts" workflow from FolderList -> AccountList
    r63068@17h:  jesse | 2009-05-10 19:11:47 -0400
    * Add required code for the 'k9beta' build
    r63069@17h:  jesse | 2009-05-10 19:12:05 -0400
    Make the folder list white backgrounded.
    r63070@17h:  jesse | 2009-05-10 19:12:26 -0400
    * Include our required libraries in build.xml
    r63071@17h:  jesse | 2009-05-10 19:13:07 -0400
    Added directories for our built code and our generated code
    r63072@17h:  jesse | 2009-05-10 19:13:36 -0400
    Added a "back" button image
    r63073@17h:  jesse | 2009-05-10 20:13:50 -0400
    Switch next/prev buttons to triangles for I18N and eventual "more easy-to-hit buttons" win
    r63074@17h:  jesse | 2009-05-10 20:17:18 -0400
    Tidy Accounts.java for some perf hacking.
    r63081@17h:  jesse | 2009-05-10 22:13:33 -0400
    First pass reformatting of the MessagingController
    r63082@17h:  jesse | 2009-05-10 23:50:28 -0400
    MessageList now correctly updates when a background sync happens
    r63083@17h:  jesse | 2009-05-10 23:50:53 -0400
    Tidying FolderList
    r63084@17h:  jesse | 2009-05-10 23:51:09 -0400
    tidy
    r63085@17h:  jesse | 2009-05-10 23:51:27 -0400
    tidy
    r63086@17h:  jesse | 2009-05-11 00:17:06 -0400
    Properly update unread counts in the FolderList after sync

    r63087@17h:  jesse | 2009-05-11 01:38:14 -0400
    Minor refactoring for readability. replace a boolean with a constant.
    r63090@17h:  jesse | 2009-05-11 02:58:31 -0400
     now that the foreground of message lists is light, we don't need the light messagebox
    r63091@17h:  jesse | 2009-05-11 17:15:02 -0400
    Added a string for "back to folder list"
    r63092@17h:  jesse | 2009-05-11 17:15:24 -0400
    Added a message list header with a back button
    r63093@17h:  jesse | 2009-05-11 17:15:54 -0400
    Remove the "folder list" button from the options menu. no sense duplicating it
    r63094@17h:  jesse | 2009-05-11 17:17:06 -0400
    Refactored views, adding our replacement scrollable header
    r63184@17h:  jesse | 2009-05-12 07:07:15 -0400
    fix weird bug where message lists could show a header element for a child
    r63185@17h:  jesse | 2009-05-12 07:08:12 -0400
    Add new-style headers to folder lists. reimplement "get folder by name" to not use a bloody for loop
    r63211@17h:  jesse | 2009-05-12 18:37:48 -0400
    Restore the former glory of the "load more messages"  widget.  it still needs an overhaul
    r63296@17h:  jesse | 2009-05-12 23:23:21 -0400
    Get the indeterminate progress bar to show up again when you click "get more messages"
    r63297@17h:  jesse | 2009-05-13 02:40:39 -0400
    Fixed off-by-one errors in click and keybindings for messagelist
    r63298@17h:  jesse | 2009-05-13 06:04:01 -0400
    Put the folder title in the name of the folderSettings popup
    r63299@17h:  jesse | 2009-05-13 06:04:49 -0400
    Reformatting. Removing debug logging
    r63300@17h:  jesse | 2009-05-13 06:05:32 -0400
    Fixing "wrong item selected" bugs in the FolderList
    r63328@17h:  jesse | 2009-05-13 13:20:00 -0400
    Update MessageView for 1.5
    r63329@17h:  jesse | 2009-05-13 13:50:29 -0400
    A couple fixes to "picking the right item"
    Titles on the message context menu
    r63330@17h:  jesse | 2009-05-13 13:58:37 -0400
    Added an "open" context menu item to the folder list
    r63347@17h:  jesse | 2009-05-13 18:00:02 -0400
    Try to get folderlists to sort in a stable way, so they jump around less in the ui
    r63349@17h:  jesse | 2009-05-13 20:37:19 -0400
    Switch to using non-message-passing based notifications for redisplay of message lists, cut down redisplay frequency to not overload the display
    r63432@17h:  jesse | 2009-05-16 13:38:49 -0400
    Android 1.5 no longer gives us apache.commons.codec by default and apache.commons.logging by default. Import them so we have em.
    There's probably something smarter to do here.
    r63438@17h:  jesse | 2009-05-16 14:12:06 -0400
    removed dead code
    r63439@17h:  jesse | 2009-05-16 14:30:57 -0400
    Minor tidy
    r63440@17h:  jesse | 2009-05-16 14:39:34 -0400
    First pass implementation making MessageList streamy for faster startup
    r63441@17h:  jesse | 2009-05-16 21:57:41 -0400
    There's no reason for the FolderList to list local messages
    r63442@17h:  jesse | 2009-05-16 21:58:57 -0400
    Switch to actually refreshing the message list after each item is loaded
    r63450@17h:  jesse | 2009-05-16 22:34:18 -0400
    Default to pulling items out of the LocalStore by date, descending. (since that's the uneditable default ordering)
    This makes our messages come out of the store in the order the user should see them
    r63451@17h:  jesse | 2009-05-16 22:34:44 -0400
    Set some new defaults for the FolderList
    r63452@17h:  jesse | 2009-05-16 22:35:43 -0400
    set some new message list item defaults
    r63456@17h:  jesse | 2009-05-17 12:56:10 -0400
    It's not clear that Pop and WebDav actually set us an InternalDate. I'd rather use that so that spam doesn't topsort. But I also want this to _work_
    r63457@17h:  jesse | 2009-05-17 12:56:47 -0400
    actually check to make sure we have a message to remove before removing it.
    r63458@17h:  jesse | 2009-05-17 13:10:07 -0400
    Flip "security type" to before the port number, since changing security type is the thing more users are likely to know/care about and resets port number
    r63469@17h:  jesse | 2009-05-17 18:42:39 -0400
    Provisional fix for "see the FoldeRList twice" bug
    r63471@17h:  jesse | 2009-05-17 20:47:41 -0400
    Remove title bar from the message view
    r63544@17h:  jesse | 2009-05-20 23:53:38 -0400
    folderlist tidying before i dig into the jumpy ordering bug
    r63545@17h:  jesse | 2009-05-20 23:56:00 -0400
    Killing dead variables
    r63546@17h:  jesse | 2009-05-21 00:58:36 -0400
    make the whole title section clicky
    r63556@17h:  jesse | 2009-05-21 01:48:13 -0400
    Fix where we go when someone deletes a message
    r63558@17h:  jesse | 2009-05-21 22:44:46 -0400
    Working toward switchable themes
    r63563@17h:  jesse | 2009-05-21 23:53:09 -0400
    Make the MessageList's colors actually just inherit from the theme, rather than hardcoding black

    r63567@17h:  jesse | 2009-05-22 10:14:13 -0400
    Kill a now-redundant comment
    r63571@17h:  jesse | 2009-05-22 19:43:30 -0400
    further theme-independence work
    r63572@17h:  jesse | 2009-05-22 19:55:23 -0400
    gete -> get (typo fix)
    r63573@17h:  jesse | 2009-05-22 22:48:49 -0400
    First cut of a global prefs system as well as a theme preference. not that it works yet
    r63577@17h:  jesse | 2009-05-24 14:49:52 -0400
    Once a user has actually put in valid user credentials, start syncing mail and folders in the background instantly.
    This gives us a much better "new startup" experience
    r63578@17h:  jesse | 2009-05-24 14:55:00 -0400
    MessageList doesn't need FolderUpdateWorker
    r63579@17h:  jesse | 2009-05-24 17:57:15 -0400
    Fix "get message by uid"
    Switch to showing messages 10 by 10, rather than 1 by 1 for huge loadtime performance improvements
    r63587@17h:  jesse | 2009-05-24 19:19:56 -0400
    Cut down LocalMessage creation to not generate a MessageId or date formatter.
    r63589@17h:  jesse | 2009-05-24 22:22:32 -0400
    Switch to null-escaping email address boundaries, rather than a VERY expensive URL-encoding
    r63590@17h:  jesse | 2009-05-24 22:23:21 -0400
    Clean up our "auto-refresh the list when adding messages after a sync"
    r63593@17h:  jesse | 2009-05-24 22:53:45 -0400
    replace isDateToday with a "rolling 18 hour window" variant that's more likely to give the user a useful answer and is 30x faster.
    r63595@17h:  jesse | 2009-05-24 23:54:14 -0400
    When instantiating messges from the LocalStore, there's no need to clear headers before setting them, nor is there a need to set a generated message id
    r63596@17h:  jesse | 2009-05-24 23:54:39 -0400
    make an overridable setGeneratedMessageId
    r63597@17h:  jesse | 2009-05-24 23:54:55 -0400
    Remove new lies from comments
    r63598@17h:  jesse | 2009-05-24 23:55:35 -0400
    Replace insanely expensive message header "name" part quoting with something consistent and cheap that does its work on the way INTO the database
    r63605@17h:  jesse | 2009-05-25 17:28:24 -0400
    bring back the 1.1 sdk build.xml
    r63606@17h:  jesse | 2009-05-25 22:32:11 -0400
    Actually enable switchable themese and compilation on 1.1
    r63692@17h:  jesse | 2009-05-29 23:55:17 -0400
    Switch back to having titles for folder and message lists.
    Restore auto-open-folder functionality
    r63694@17h:  jesse | 2009-05-30 18:50:39 -0400
    Remove several off-by-one errors introduced by yesterday's return to android titlebars
    r63696@17h:  jesse | 2009-05-30 23:45:03 -0400
    use convertView properly for performance and memory imrpovement in FolderList and MessageList
    r63698@17h:  jesse | 2009-05-31 19:42:59 -0400
    Switch to using background shading to indicate "not yet fetched"
    r63701@17h:  jesse | 2009-05-31 21:28:47 -0400
    Remving code we don't actually need these bits of apache commons on 1.1