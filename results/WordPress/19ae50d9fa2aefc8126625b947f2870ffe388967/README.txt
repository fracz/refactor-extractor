commit 19ae50d9fa2aefc8126625b947f2870ffe388967
Author: James Nylen <jnylen@gmail.com>
Date:   Wed Feb 1 20:32:04 2017 +0000

    REST API: Improve posts orderby tests

    This commit adds tests for `orderby=relevance` combined with a search term in the REST API.

    It also improves tests for the `orderby` parameter in `WP_REST_Posts_Controller` by looking at the generated SQL query instead of creating a bunch of carefully arranged test objects.  This should be much more robust, and we can use this approach in other places (such as #39055).

    Fixes #39079.

    Built from https://develop.svn.wordpress.org/trunk@40037


    git-svn-id: http://core.svn.wordpress.org/trunk@39974 1a063a9b-81f0-0310-95a4-ce76da25c4cd