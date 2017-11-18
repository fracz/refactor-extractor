commit 1a48babb5ed30e8a3eb9645355d6455b21041406
Author: Selim Cinek <cinek@google.com>
Date:   Fri Feb 17 19:38:40 2017 -0800

    Move the inflation away from the statusbar

    Since the notifications need to inflate more dynamically
    based on their own state, the inflation is moved away
    from the statusbar.
    This also improves the apply inplace logic, that was
    reinflating all views even if only a single notification
    layout was changed.

    Test: runtest systemui
    Bug: 35125708
    Change-Id: I42a33065ab60b7c45ca979ae2d7baa1518bf92b7