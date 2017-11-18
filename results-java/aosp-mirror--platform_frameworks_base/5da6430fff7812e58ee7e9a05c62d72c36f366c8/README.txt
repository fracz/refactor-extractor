commit 5da6430fff7812e58ee7e9a05c62d72c36f366c8
Author: Adam Powell <adamp@google.com>
Date:   Mon Nov 5 14:16:59 2012 -0800

    Optimize keyguard/IME interactions

    Change the keyguard window to LAYOUT_IN_SCREEN | LAYOUT_INSET_DECOR
    and make the ViewManagerHost fitSystemWindows. This eliminates the
    need to resize the actual window and associated surfaces when the IME
    comes and goes.

    Force the widget pager to measure at the fullscreen size of the
    keyguard, even if the IME is showing. This causes the widgets to clip
    instead of resize, removing a few more moving parts that can be
    distracting/affect performance.

    Partially improves bug 7427586

    Change-Id: I0d86d0ca8045f737fa97ca5d1e7f6a49f746b999