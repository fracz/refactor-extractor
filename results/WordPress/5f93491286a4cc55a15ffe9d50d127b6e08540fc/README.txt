commit 5f93491286a4cc55a15ffe9d50d127b6e08540fc
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Wed Nov 11 00:24:28 2015 +0000

    Accessibility: improvements for the taxonomies Quick Edit form.

    Changes the "Cancel" and "Update" controls in buttons for better semantics and
    accessibility. On cancel and successful saving, moves focus back to the term title
    to avoid a focus loss. Dispatches error and success messages to `wp.a11y.speak`
    to give assistive technologies users an audible feedback.

    Patch prepared at #wpcdit, first Italian WordPress Contributor Day.

    Props garusky, chiara_09.
    Fixes #34613.
    Built from https://develop.svn.wordpress.org/trunk@35605


    git-svn-id: http://core.svn.wordpress.org/trunk@35569 1a063a9b-81f0-0310-95a4-ce76da25c4cd