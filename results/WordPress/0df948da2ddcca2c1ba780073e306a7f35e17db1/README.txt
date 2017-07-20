commit 0df948da2ddcca2c1ba780073e306a7f35e17db1
Author: Jeremy Felt <jeremy.felt@gmail.com>
Date:   Wed Apr 27 06:33:27 2016 +0000

    Tests: Reduce unnecessary count in `create_many()` in multisite user tests

    The tests for `is_blog_user()` and `is_user_member_of_blog()` should be refactored. Until then, we can shave several seconds from the test time by avoiding unnecessary loops of the same tests.

    See #36566.

    Built from https://develop.svn.wordpress.org/trunk@37318


    git-svn-id: http://core.svn.wordpress.org/trunk@37284 1a063a9b-81f0-0310-95a4-ce76da25c4cd