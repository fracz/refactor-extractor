commit 95bb449a4dca3b5cea94bee89fd3f8e54090affd
Author: Drew Jaynes <info@drewapicture.com>
Date:   Sun Jan 5 17:55:11 2014 +0000

    Inline documentation improvements for get_*_template() functions in wp-includes/template.php.

    * Adds `@see` references for `get_query_template()` or similar
    * Adds complete `@return` descriptions
    * Clarifies which dynamic hooks can be used to filter specific template types

    Props UmeshSingla for the initial patches. Props SergeyBiryukov, DrewAPicture.
    Fixes #26742.

    Built from https://develop.svn.wordpress.org/trunk@26906


    git-svn-id: http://core.svn.wordpress.org/trunk@26789 1a063a9b-81f0-0310-95a4-ce76da25c4cd