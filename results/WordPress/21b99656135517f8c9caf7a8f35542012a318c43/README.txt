commit 21b99656135517f8c9caf7a8f35542012a318c43
Author: Dominik Schilling <dominikschilling+git@gmail.com>
Date:   Sat Feb 20 14:41:27 2016 +0000

    Theme Compat: Replace the custom comment form with `comment_form()` and reduce number of links.

    `comment_form()` has nearly the same markup as the custom form but also includes the latest enhancements like improved a11y and more filters.

    Add translators comments, props ramiy.

    Fixes #35888.
    Built from https://develop.svn.wordpress.org/trunk@36595


    git-svn-id: http://core.svn.wordpress.org/trunk@36562 1a063a9b-81f0-0310-95a4-ce76da25c4cd