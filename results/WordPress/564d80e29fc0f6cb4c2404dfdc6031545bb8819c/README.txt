commit 564d80e29fc0f6cb4c2404dfdc6031545bb8819c
Author: Ryan Boren <ryan@boren.nu>
Date:   Thu Aug 15 20:09:09 2013 +0000

    wp_get_shortlink() improvements.

    * Return shortlinks for pages and public CPTs.
    * Return shortlinks even when cruft-free links are not enabled.
    * Unit tests

    Props sillybean, layotte, cais
    fixes #18632
    see #14760


    Built from https://develop.svn.wordpress.org/trunk@25030


    git-svn-id: http://core.svn.wordpress.org/trunk@25017 1a063a9b-81f0-0310-95a4-ce76da25c4cd