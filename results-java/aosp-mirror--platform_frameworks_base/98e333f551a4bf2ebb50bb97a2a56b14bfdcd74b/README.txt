commit 98e333f551a4bf2ebb50bb97a2a56b14bfdcd74b
Author: Karl Rosaen <krosaen@google.com>
Date:   Tue Apr 28 10:39:09 2009 -0700

    Fix back key and ime behavior for search dialog.

    The back key now dismisses the soft keyboard, and then the dialog.

    The soft keyboard behavior is improved by having ACTV do the following when 'mDropdownAlwaysShowing' is true:
    - touching outside of the drop down doesn't dismiss it
    - touching the text field ensures the imei is brought in front of the drop down