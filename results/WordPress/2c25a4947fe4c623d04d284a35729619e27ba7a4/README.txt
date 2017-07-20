commit 2c25a4947fe4c623d04d284a35729619e27ba7a4
Author: Jeremy Felt <jeremy.felt@gmail.com>
Date:   Tue Dec 2 22:56:22 2014 +0000

    Split and improve multisite tests for upload quota

    Break a single test with many assertions into many tests with single assertions.

    In the process, provide separate and comprehensive tests for `upload_is_user_over_quota()`, `is_upload_space_available()`, and `get_space_allowed()`.

    Also removes a check for `BLOGSUPLOADDIR`, a constant that never existed. New tests will need to be introduced to handle the ms-files group.

    See #30080

    Built from https://develop.svn.wordpress.org/trunk@30715


    git-svn-id: http://core.svn.wordpress.org/trunk@30705 1a063a9b-81f0-0310-95a4-ce76da25c4cd