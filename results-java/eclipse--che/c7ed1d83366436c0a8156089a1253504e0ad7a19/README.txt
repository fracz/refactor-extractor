commit c7ed1d83366436c0a8156089a1253504e0ad7a19
Author: Mykola Morhun <mmorhun@redhat.com>
Date:   Tue Sep 12 14:13:52 2017 +0300

    Add ability to switch between files in Git Diff widget (#5965)

    * Performs small refactoring of git-compare-related functionality.
    * Adds ability to switch to the next/previous file in git compare widget.
    * Adds hotkeys for next and previous diff
    * Adds Save Changes button for git compare widget.
    * Fixes compare with deleted file bug.
    * Moves Git Diff widget from iframe to IDE.