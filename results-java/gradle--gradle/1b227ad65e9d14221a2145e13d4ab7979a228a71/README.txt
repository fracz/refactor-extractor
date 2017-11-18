commit 1b227ad65e9d14221a2145e13d4ab7979a228a71
Author: Cedric Champeau <cedric@gradle.com>
Date:   Tue Sep 12 09:36:46 2017 +0200

    Revert "Rename `VersionSelector` to `VersionMatcher`"

    This reverts commit 5046159bfc739c3d86c59411dd4b4a1c36786b81. The Nebula resolution rules plugin makes use of internal classes,
    and broke with this refactoring.