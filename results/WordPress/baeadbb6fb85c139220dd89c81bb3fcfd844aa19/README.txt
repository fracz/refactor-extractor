commit baeadbb6fb85c139220dd89c81bb3fcfd844aa19
Author: Dominik Schilling <dominikschilling+git@gmail.com>
Date:   Sat Jun 20 19:22:25 2015 +0000

    Customizer: Accessibility improvements for menu item searches.

    * Add a description and an `aria-describedby` attribute to inform users this is a "live" search.
    * Announce the number of search results via `wp.a11y.speak`.
    * Use `aria-busy` attribute to stop screen readers announcing content while the "loading more results" is running.
    * Increase the search debounce wait interval to 500ms to be consistent with other live searches.
    * If a search fails don't call `wp.a11y.speak` with an undefined variable.

    props afercia.
    see #32720.
    Built from https://develop.svn.wordpress.org/trunk@32891


    git-svn-id: http://core.svn.wordpress.org/trunk@32862 1a063a9b-81f0-0310-95a4-ce76da25c4cd