commit 5a2b91dc14e4c92e91c6abcc795f54ac98ee5866
Author: Mitsuru Oshima <oshima@google.com>
Date:   Thu Jul 16 16:30:02 2009 -0700

     * Use Fede In/Out animation if one of opening/closing apps is in compatibility mode.
     * preserve compatibility window flag when the app updates window's layout params.
     * Added assertion in DEFAULT_COMPATIBILITY_INFO object to prevent unintentional modification.
     * A few minor updates
        * log/dump message improvement
        * Removed unnecessary method in FadeInOutAnimator
        * Fixed 100 char issue in WindwoManagerServer.java