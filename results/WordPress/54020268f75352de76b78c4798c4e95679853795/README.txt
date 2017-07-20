commit 54020268f75352de76b78c4798c4e95679853795
Author: Drew Jaynes <info@drewapicture.com>
Date:   Thu Oct 30 02:51:23 2014 +0000

    Update the changelog for `get_approved_comments()` to reflect that the function was refactored to leverage `WP_Comment_Query` in [30098].

    Also updates the `$args` parameter description with a reference to see `WP_Comment_Query` for argument information.

    See #12668.

    Built from https://develop.svn.wordpress.org/trunk@30109


    git-svn-id: http://core.svn.wordpress.org/trunk@30109 1a063a9b-81f0-0310-95a4-ce76da25c4cd