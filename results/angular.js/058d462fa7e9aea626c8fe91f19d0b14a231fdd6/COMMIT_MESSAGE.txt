commit 058d462fa7e9aea626c8fe91f19d0b14a231fdd6
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Mon Jul 27 21:55:15 2015 +0100

    revert: refactor($locale): use en-us as generic built-in locale

    This reverts commit 70ce425e6a5c9d18caab754d03f6494b5174774e.

    There are internal projects in Google that generate their own version
    of angular.js and so this commit caused those projects to break.

    We are going to look into a more satisfactory way of getting this change
    in.