commit e4987beba316a1a9f03ac08720dee680e4ff4b07
Author: Drew Jaynes <info@drewapicture.com>
Date:   Mon Sep 23 04:16:08 2013 +0000

    Inline documentation improvements for wp-includes/comment-template.php.

    Changes include:
    - Existing phpdoc block improvements
    - Inclusion of defaults for optional parameters
    - Hash-notated argument arrays
    - Removal of unnecessary or redundant tagging.

    Also standardization of int|WP_Post function parameters.

    These changes clear the way toward less redundancy for hook docs in a future ticket.

    Still left: Fully documented arguments in `comment_form()` and a pass through Walker_Comment.

    See #20495. See #25388.

    Built from https://develop.svn.wordpress.org/trunk@25567


    git-svn-id: http://core.svn.wordpress.org/trunk@25485 1a063a9b-81f0-0310-95a4-ce76da25c4cd