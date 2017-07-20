commit 5eae48b414e6f30dbd96ff44474eb623a4e93d11
Author: Dominik Schilling <dominikschilling+git@gmail.com>
Date:   Fri Jul 8 11:27:27 2016 +0000

    Boostrap: Move `wp_convert_hr_to_bytes()` to wp-includes/load.php.

    `wp_convert_hr_to_bytes()` was previously defined in wp-includes/media.php because it's only used by `wp_max_upload_size()` in the same file.
    Moving this function to load.php allows us to improve core's memory limit handling.

    See #32075.
    Built from https://develop.svn.wordpress.org/trunk@38012


    git-svn-id: http://core.svn.wordpress.org/trunk@37953 1a063a9b-81f0-0310-95a4-ce76da25c4cd