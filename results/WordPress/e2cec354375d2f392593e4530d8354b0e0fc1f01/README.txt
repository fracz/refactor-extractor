commit e2cec354375d2f392593e4530d8354b0e0fc1f01
Author: Drew Jaynes <info@drewapicture.com>
Date:   Sun Feb 7 02:13:26 2016 +0000

    Docs: Make some minor improvements to inline docs for `WP_Site`, introduced in [36393].

    * Uses third-person singular verbs in method summaries
    * Adds an `@static` tag to the `WP_Site::get_instance()` DocBlock
    * Adjusts return types for `WP_Site::get_instance()` to the more explicit `WP_Site|false`

    See #32450. See #32246.

    Built from https://develop.svn.wordpress.org/trunk@36495


    git-svn-id: http://core.svn.wordpress.org/trunk@36462 1a063a9b-81f0-0310-95a4-ce76da25c4cd