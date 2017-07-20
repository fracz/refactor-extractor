commit d242446e599ce79a61ee6180613b4ffcf83e92c0
Author: Jeremy Felt <jeremy.felt@gmail.com>
Date:   Fri Jun 17 00:03:29 2016 +0000

    Multisite: Use `WP_Site_Query` to power `WP_MS_Sites_List_Table`.

    `WP_Site_Query` provides for a cleaner `prepare_items()` method. It significantly improves the search experience in the sites list table:

    * In a subdomain configuration, domain and path are searched for a provided terms.
    * In a subdirectory configuration, path is searched for a provided term.
    * The full domain is searched in a subdomain configuration rather than the portion not matching the network's domain.
    * Terms are searched as `%term%` by default. Adding `*` in the middle of a term will search `%te%rm%`.

    Props flixos90, Fab1en.
    Fixes #33185, #24833, #21837, #36675.

    Built from https://develop.svn.wordpress.org/trunk@37736


    git-svn-id: http://core.svn.wordpress.org/trunk@37701 1a063a9b-81f0-0310-95a4-ce76da25c4cd