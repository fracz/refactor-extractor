commit e69c93181f1f313dcedd07f677af1cea953fdf16
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Mon Oct 31 18:24:38 2016 -0700

    The big Keyguard transition refactor (8/n)

    Don't force mKeyguardGoingAway, as this never recovers. Make sure
    to only show the dismissing Keyguard activtiy and recover the
    state when trusted state changes.

    Test: Make sure Keyguard is in a trusted state, start an activity
    with FLAG_DISMISS_KEYGUARD from FLAG_SHOW_WHEN_LOCKED activity
    and make sure there is no flicker.

    Bug: 32057734
    Change-Id: I5d212f6f9d5430250b22c8370f45dc95756432d2