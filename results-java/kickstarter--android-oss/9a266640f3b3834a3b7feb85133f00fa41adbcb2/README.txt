commit 9a266640f3b3834a3b7feb85133f00fa41adbcb2
Author: lisa luo <luoser@users.noreply.github.com>
Date:   Thu Jul 20 11:01:52 2017 -0400

    [Messages] Sweep before release (#131)

    * take 1 to prevent reload on rotation

    * minor cleanups

    * Cleanup layouts a bit

    * Scroll to bottom when new message is sent

    * Collapse toolbar when keyboard opens for threads with messages

    * Add white border to avatar image

    * update config n strings from server

    * use new message reply hint

    * Append new message to correct last position

    * refactor to preserve message list on rotation

    * remove these to fix weird icon bug

    * Add Mailbox enum logic

    * refresh user on swipe refresh as well

    * enable scrollbars on message threads RV

    * some a11y

    * set the default animation style...

    * not the best not the worst way to fix initial bottom padding

    * fix margin

    * clearer dimen name

    * cleanup logic, add test

    * change to PS so activity doesnt start again on rotate