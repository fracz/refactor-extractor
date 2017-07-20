commit 67e1f55c4bc1e647ff776ceaac08f37065cfcfdb
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Sat Dec 19 00:17:26 2015 +0000

    Accessibility: Remove title attributes and improve accessibility on the "no-js" Menus screen.

    When JavaScript is off, the reorder menu item and Edit menu item links now use `aria-label`
    attributes instead of title attributes.

    Fixes #35134.
    Built from https://develop.svn.wordpress.org/trunk@36016


    git-svn-id: http://core.svn.wordpress.org/trunk@35981 1a063a9b-81f0-0310-95a4-ce76da25c4cd