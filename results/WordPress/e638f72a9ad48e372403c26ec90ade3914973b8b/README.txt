commit e638f72a9ad48e372403c26ec90ade3914973b8b
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Tue Dec 22 01:51:28 2015 +0000

    Order terms by 'name' when populating object term cache.

    [34217] removed the `ORDER BY` clause from `update_object_term_cache()`, for
    improved performance. But this proved to cause problems in cases where users
    were expecting the results of `get_the_terms()` to be ordered by 'name'. Let's
    revert the change for the time being, and look into more disciplined ordering
    in a future release.

    Props afercia.
    See #28922. Fixes #35180.
    Built from https://develop.svn.wordpress.org/trunk@36056


    git-svn-id: http://core.svn.wordpress.org/trunk@36021 1a063a9b-81f0-0310-95a4-ce76da25c4cd