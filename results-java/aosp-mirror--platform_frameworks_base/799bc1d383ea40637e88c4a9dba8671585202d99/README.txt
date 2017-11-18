commit 799bc1d383ea40637e88c4a9dba8671585202d99
Author: Craig Mautner <cmautner@google.com>
Date:   Wed Jan 14 10:33:48 2015 -0800

    Refactor moveStackWindowsLocked()

    The method had multiple inner loops and was a less efficient form of
    rebuildAppWindowsLocked(). Rewritten to use rebuildAppWindowsLocked()
    and small other refactors.

    Item #1 of bug 18088522.

    Change-Id: If93fa961922c77c9f0af719e535ae5ca5d30fe59