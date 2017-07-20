commit b1c2305aa1630723ded27408a63611ac178fa73e
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Thu May 28 16:06:27 2015 +0000

    Improve tests for `is_email_address_unsafe()`.

    * Move to a separate file for better organization and method names.
    * Use a `dataProvider` when appropriate, for better readability.
    * Add a test for splitting the banned domain list on line breaks.

    See #20459, #21730.
    Built from https://develop.svn.wordpress.org/trunk@32638


    git-svn-id: http://core.svn.wordpress.org/trunk@32608 1a063a9b-81f0-0310-95a4-ce76da25c4cd