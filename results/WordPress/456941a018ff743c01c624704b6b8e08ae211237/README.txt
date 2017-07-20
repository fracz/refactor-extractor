commit 456941a018ff743c01c624704b6b8e08ae211237
Author: Weston Ruter <weston@xwp.co>
Date:   Wed Nov 23 17:34:31 2016 +0000

    Customize: Refactor logic for updating `custom_css` posts by introducing `wp_update_custom_css_post()` function and renaming update filter.

    * Moves logic from `WP_Customize_Custom_CSS_Setting::update()` into a re-usable `wp_update_custom_css_post()` function, useful for future REST API endpoint, WP-CLI command, or plugin migrations.
    * Renames `customize_update_custom_css_post_content_args` filter to `update_custom_css_data` and improves the naming of the parameters. Instead of passing `post_content` and `post_content_filtered` the filtered array now contains `css` and `preprocessed` respectively.
    * The second context param for the `update_custom_css_data` filter is now an array of the original args passed to `wp_update_custom_css_post()` and there is now no more `$setting` arg since it isn't necessarily being called in the customizer context.

    Props westonruter, georgestephanis.
    See #35395.
    Fixes #38672.

    Built from https://develop.svn.wordpress.org/trunk@39350


    git-svn-id: http://core.svn.wordpress.org/trunk@39290 1a063a9b-81f0-0310-95a4-ce76da25c4cd