commit 246529891ee289e8393ad4a486db785ef455c778
Author: Amith Yamasani <yamasani@google.com>
Date:   Thu Jun 23 16:16:05 2011 -0700

    SearchView improvements per design.

    - X is visible only if there is text, or we need a way to close a
    search field that is iconified by default.
    - Search dialog (legacy) has a back button to the left.
    - Hitting X on a non-focused search view will bring it into focus
    and show the keyboard if necessary.

    Change-Id: I5a30bb08adcf84639a922a9e13be1d1562f714e6