commit 22937c9fc71beb045384417fe226504b1f003470
Author: Ben Kwa <kenobi@google.com>
Date:   Tue Feb 23 23:00:01 2016 -0800

    Type-to-focus improvements.

    - Fix handling of tab and backspace keys while in type-to-search mode.
    - Fix search term highlighting on directory items in grid view.
    - Switch to a time-based search model.  Typing starts a search.  The
      current search expires if no keys are pressed for 500 ms.
    - Make type-to-focus case-insensitive.

    BUG=27336167
    BUG=27324974

    Change-Id: I92633222c9a04af24729501c48885eabebf00696