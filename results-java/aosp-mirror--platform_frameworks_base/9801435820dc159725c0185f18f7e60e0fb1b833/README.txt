commit 9801435820dc159725c0185f18f7e60e0fb1b833
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Apr 5 18:31:41 2012 -0700

    Fix so that status bar doesn't resize when hiding nav bar.

    The status bar now extends behind the nav bar, and uses
    fitsSystemWindows to ensure its content is not covered.  We
    always report a stable content insets (as if the nav bar is
    visible) even if the nav bar is hidden, so the content doesn't
    jump when transitioing.  This does mean that if you only hide
    the nav bar (and not the status bar), when in landscape you
    will end up with a status bar whose right side still leaves
    room for the nav bar.  But why the hell would you want to do
    that?

    Also improve documentation on setSystemUiVisibility().

    Change-Id: I8087d875f1214ef0085a91b5ed5c2f35ff2fc1b3