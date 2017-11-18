commit 6e94a9eb8709ddd3127e3c0103cd6a0f550f6e74
Author: Wale Ogunwale <ogunwale@google.com>
Date:   Wed Oct 7 15:35:49 2015 -0700

    Added rules for status/nav bar customization when in multi-window mode

    Status & nav bars are always visible and opaque when freeform or docked
    stack is visible

    We still need to refactor the code to allow force opaque to be updated
    independently for the status bar and nav. bar. Once that is done, the
    status bar should be transcluent if freeform stack is visible and
    docked stack isn't and the status bar should be opaque if freeform
    & docked stack are both visible.

    Bug: 24365214
    Change-Id: I48ecc6067c9b0f7239c12c98eb46f3fcefacd4f9