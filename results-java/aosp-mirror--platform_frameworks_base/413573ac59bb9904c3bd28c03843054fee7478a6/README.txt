commit 413573ac59bb9904c3bd28c03843054fee7478a6
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Mon Feb 22 17:52:45 2016 -0700

    Offer to cache ringtones in system DE storage.

    Ringtones often live on shared media, which is now encrypted with CE
    keys and not available until after the user is unlocked.  To improve
    the user experience while locked, cache the default ringtone,
    notification sound, and alarm sound in a DE storage area.

    Bug: 26730753
    Change-Id: Ie6ad7790af4c87dd25759df3ed017e3b91a2fb87