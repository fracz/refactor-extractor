commit a2ef00b4282dcf122c3bdd6fc23cf772474f6abb
Author: Amith Yamasani <yamasani@google.com>
Date:   Thu Jul 30 16:14:34 2009 -0700

    Don't invalidate view if setEnabled doesn't change the state.

    Check the current enabled state before setting it, in case there's
    no change. Otherwise some apps are repeatedly redrawing buttons based
    on validation of some text field (like gmail or mms message bodies).
    Should slightly improve the performance of soft keyboard text entry.