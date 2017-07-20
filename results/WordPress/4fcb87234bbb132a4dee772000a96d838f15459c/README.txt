commit 4fcb87234bbb132a4dee772000a96d838f15459c
Author: Mark Jaquith <mark@wordpress.org>
Date:   Wed Nov 21 07:08:38 2012 +0000

    Remove trailing slashes from UPLOADS before trying a str_replace() in wp_upload_dir(). props jbrinley. fixes #22469

    * In 3.4.x, both $url and UPLOADS had trailing slashes
    * Due to refactoring, $url is no longer expected to have a trailing slash
    * Because of the mismatch, the str_replace() was not working, resulting in an incorrectly verbose upload dir URL

    git-svn-id: http://core.svn.wordpress.org/trunk@22736 1a063a9b-81f0-0310-95a4-ce76da25c4cd