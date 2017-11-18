commit c9102d743f5ad2e4ce97ca347da15410ff6413ba
Author: Adam Powell <adamp@google.com>
Date:   Wed Jun 4 15:49:04 2014 -0700

    Fix list navigation callbacks for ActionBarView

    Fix a regression from a previous refactoring where the call ordering
    between setNavigationMode and setListNavigationCallbacks could result
    in the item selection listener being ignored.

    Bug 15395053

    Change-Id: I5330f2c684a21448c64c6b62eec10b96405758f7