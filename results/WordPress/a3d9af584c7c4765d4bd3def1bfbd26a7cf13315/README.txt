commit a3d9af584c7c4765d4bd3def1bfbd26a7cf13315
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Mon Jun 29 14:16:26 2015 +0000

    Introduce 'wp_generate_tag_cloud_data' filter.

    This filter allows developers to modify the data that is used to create tag
    clouds, without having to manipulate the tag cloud markup directly.

    As part of the refactor, this changeset also adds a few unit tests for the way
    `wp_generate_tag_cloud()` generates the 'title' attribute, as well as
    improvements to output escaping.

    Props flixos90, ysalame.
    Fixes #24656.
    Built from https://develop.svn.wordpress.org/trunk@32996


    git-svn-id: http://core.svn.wordpress.org/trunk@32967 1a063a9b-81f0-0310-95a4-ce76da25c4cd