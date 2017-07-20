commit 43b722b8fc41af49931ada60d50eb906883b1ab0
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Wed Feb 24 16:07:26 2016 +0000

    Accessibility: improve accessibility of the Dashboard "Recent Comments" widget.

    - Makes the list of comments a list
    - Always displays the title of the post the comment relates to, linked to the post itself and no more to the Edit screen
    - Headings: changes the visible one in "Recent Comments" and adds a hidden "View more comments" heading before the views links
    - Adds the pending status indicator to Pingbacks and Trackbacks

    Props rachelbaker, afercia.

    Fixes #35392.
    Built from https://develop.svn.wordpress.org/trunk@36683


    git-svn-id: http://core.svn.wordpress.org/trunk@36650 1a063a9b-81f0-0310-95a4-ce76da25c4cd