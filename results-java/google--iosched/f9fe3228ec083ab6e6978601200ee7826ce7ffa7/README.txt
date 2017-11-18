commit f9fe3228ec083ab6e6978601200ee7826ce7ffa7
Author: Nick Butcher <nickbutcher@google.com>
Date:   Wed Apr 20 15:43:58 2016 +0100

    Updated Session Details screen to use modern components.

    Replace custom scroll tracking with components from the design
    support library. This allows us to remove a lot of custom code.

    CollapsingToolbarLayout in particular makes it simple to draw
    the session photo behind status bar on API 23+.

    Also improved the shared element transitions.

    Bug: 28208316
    Bug: 27786714
    Change-Id: Ic2e4ee503d334d876a04ea29663c8bf725e8da53