commit fa6e0a80e20a4917b403fd330e01a9edd021aabc
Author: Yohei Yukawa <yukawa@google.com>
Date:   Thu Jul 23 15:08:59 2015 -0700

    Don't rely on broadcast intent for waking up input method.

    Basically this is a copy of Iabef96921dd554ce3768fb18619cefc
    for InputMethodManagerService.

    As described in JavaDoc of Intent#ACTION_SCREEN_OFF and
    Intent#ACTION_SCREEN_ON, one can use those Intents to be
    notified when the device becomes non-interactive and
    interactive.  IMMS has relied on them to enable and disable
    InputConnection between the IME and the application so as not
    to allow IMEs to update text when the user does not present.
    This is actually our design goal as documented in JavaDoc of
    InputMethodManager.

       An IME can never interact with an InputConnection while
       the screen is off.  This is enforced by making all clients
       inactive while the screen is off, and prevents bad IMEs from
       driving the UI when the user can not be aware of its
       behavior.

    The goal of this CL is to improve the timeliness of above
    mechianism by introducing a direct communication channel from
    PowerManagerService to InputMethodManagerService via Notifier.
    Actually this is what InputManager has been doing since
    Iabef96921dd554ce3768fb18619cefc3230b5fb0.

    Reasons behind this change are:

      1. There are several bugreports that imply those Intents can
         dispatch tens of seconds after it is enqueued. This is
         indeed problematic because the user cannot type password
         to unlock their devices until queued
         Intent#ACTION_SCREEN_ON is dispatched. This CL addresses
         such an issue without waiting for figuring out the root
         cause of the delay.
      2. Intent#ACTION_SCREEN_OFF and Intent#ACTION_SCREEN_ON are
         sent as a ordered broadcast, which may not be suitable for
         tasks that require a certain level of timeliness, and what
         IMMS wants is to enable users to start typing immediately
         after the system.

    This CL was originally authored by Seigo Nonaka.

    Bug: 22423200
    Bug: 22555778
    Change-Id: I747c37ff6dd8f233faef43f2b5713a4320e848eb