commit d3463e4aea3b958c428397de3622b15eddb31158
Author: Ryan Boren <ryan@boren.nu>
Date:   Fri Nov 9 19:38:54 2012 +0000

    WP_Image_Editor improvements.

    * Make test() and supports_mime_type() static.
    * Add required_methods argument to get_instance(). Allows requesting an implementation that has certain methods/capabilities.
    * Whitespace cleanup

    Props markoheijnen
    see #6821


    git-svn-id: http://core.svn.wordpress.org/trunk@22510 1a063a9b-81f0-0310-95a4-ce76da25c4cd