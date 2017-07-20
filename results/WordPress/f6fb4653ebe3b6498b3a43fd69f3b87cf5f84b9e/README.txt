commit f6fb4653ebe3b6498b3a43fd69f3b87cf5f84b9e
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Wed Sep 16 22:00:25 2015 +0000

    Don't notify post authors about spam comments.

    [34106] moved post author notification to a hook, and in the process, missed
    the 'spam' check. This changeset restores that check.

    To make unit testing easier, the notification callbacks have been refactored
    to return values: false when various conditions aren't met (eg, approved
    comments should not trigger moderation emails), and the return value of the
    `wp_notify_*()` function otherwise.

    Props cfinke, kraftbj.
    See #33587.
    Built from https://develop.svn.wordpress.org/trunk@34250


    git-svn-id: http://core.svn.wordpress.org/trunk@34214 1a063a9b-81f0-0310-95a4-ce76da25c4cd