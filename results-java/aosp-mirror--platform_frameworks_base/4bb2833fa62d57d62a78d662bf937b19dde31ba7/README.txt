commit 4bb2833fa62d57d62a78d662bf937b19dde31ba7
Author: Gilles Debunne <debunne@google.com>
Date:   Mon Jan 23 18:35:32 2012 -0800

    Restored selectAllOnFocus

    Bug introduced in recent refactoring
    https://android-git.corp.google.com/g/#/c/158896/

    Do not move cursor when selectAllOnFocus and focus just happened.
    The didTouchFocusSelect() condition was not copied over from ArrowKeyMM.

    Change-Id: Id01d225c436ae3dd97c5d77d5dac5d0690d7de76