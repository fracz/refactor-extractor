commit 61d21cc0d9c4439a33dddbbcb46f869fa838ce2b
Author: Andrew Ozz <admin@laptoptips.ca>
Date:   Wed Oct 28 05:41:24 2015 +0000

    Responsive images:
    - Introduce `wp_calculate_image_srcset()` that replaces `wp_get_attachment_image_srcset_array()` and is used as lower level function for retrieving the srcset data as array.
    - Use the new function when generating `srcset` and `sizes` on the front-end. This is faster as no (other) image API functions are used.
    - Change the `wp_get_attachment_image_srcset()`. Now it is meant for use in templates and is no longer used in core.
    - A few logic fixes and improvements.
    - Some names changed to be (hopefully) more descriptive.
    - Fixed/updated tests.

    Props joemcgill, jaspermdegroot, azaozz.
    See #34430.
    Built from https://develop.svn.wordpress.org/trunk@35412


    git-svn-id: http://core.svn.wordpress.org/trunk@35376 1a063a9b-81f0-0310-95a4-ce76da25c4cd