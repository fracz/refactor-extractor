commit ef8f9168b6ce85d7c2c4994d771464db30bd9be0
Author: Pascal Birchler <pascal.birchler@gmail.com>
Date:   Fri Oct 7 17:12:29 2016 +0000

    Taxonomy: Introduce `WP_Taxonomy` and use it in `register_taxonomy()` and `unregister_taxonomy()`.

    This changes the global `$wp_taxonomies` to an array of `WP_Taxonomy ` objects. `WP_Taxonomy ` includes methods to handle rewrite rules and hooks.
    Each taxonomy argument becomes a property of `WP_Taxonomy`. Introducing such a class makes further improvements in the future much more feasible.

    Props boonebgorges for review.
    Fixes #36224. See #36217.
    Built from https://develop.svn.wordpress.org/trunk@38747


    git-svn-id: http://core.svn.wordpress.org/trunk@38690 1a063a9b-81f0-0310-95a4-ce76da25c4cd