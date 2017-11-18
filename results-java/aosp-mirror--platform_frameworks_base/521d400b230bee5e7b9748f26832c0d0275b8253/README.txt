commit 521d400b230bee5e7b9748f26832c0d0275b8253
Author: Jim Miller <jaggies@google.com>
Date:   Sun Nov 15 16:19:24 2009 -0800

    Fix 2209086: Add animations and misc ui improvements to SlidingTab.

    Added translation animation where tab "flies" when you release.
    Added translation animation where pressing and releasing one tab will hide/show the other
    Added alpha animation to make target appear gradually
    Added margin around swipe area to allow easier unlocking.
    Removed unused handler.