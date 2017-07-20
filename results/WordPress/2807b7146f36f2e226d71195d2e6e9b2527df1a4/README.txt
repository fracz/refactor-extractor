commit 2807b7146f36f2e226d71195d2e6e9b2527df1a4
Author: Scott Taylor <scott.c.taylor@mac.com>
Date:   Thu Apr 30 21:40:25 2015 +0000

    After [32258], restore the parts of [31620] and [31626] that weren't changes to the UI, but were improvements to existing code.

    * Use `wp.shortcode()` instead of manually constructing a shortcode in `views/embed/link`
    * In `WP_Embed`, store the last URL and last set of attributes requested in class properties
    * `wp_ajax_parse_embed()`, allow `[embed]`s to have attributes. Return `attr` in the response.

    See #31139.

    Built from https://develop.svn.wordpress.org/trunk@32330


    git-svn-id: http://core.svn.wordpress.org/trunk@32301 1a063a9b-81f0-0310-95a4-ce76da25c4cd