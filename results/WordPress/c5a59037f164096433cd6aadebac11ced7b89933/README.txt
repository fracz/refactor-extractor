commit c5a59037f164096433cd6aadebac11ced7b89933
Author: Drew Jaynes <info@drewapicture.com>
Date:   Mon Jul 14 01:02:15 2014 +0000

    Inline documentation cleanup for 4.0 audit.

    Various improvements:
    * Adds `@see` reference for `wp_list_comments()` in 'wp_list_comments_args' filter docs, added in [28285]
    * Various phpDoc tweaks for the 'run_wptexturize' filter docs, added in [28715]
    * Sentence and wrapping changes for `is_https_url()`, added in [28894]
    * Documents the `$args` parameter for `wp_dropdown_languages()`, added in [29007]
    * Adds a period to the parameter description for `_update_posts_count_on_delete()`, added in [28835]
    * Documents a global in `is_customize_preview()`, added in [28999]
    * phpDoc tweaks, adds an access modifier for `wpdb::esc_like()`, added in [28711]

    See #28885.

    Built from https://develop.svn.wordpress.org/trunk@29163


    git-svn-id: http://core.svn.wordpress.org/trunk@28947 1a063a9b-81f0-0310-95a4-ce76da25c4cd