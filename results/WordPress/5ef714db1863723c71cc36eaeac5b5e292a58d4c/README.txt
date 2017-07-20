commit 5ef714db1863723c71cc36eaeac5b5e292a58d4c
Author: Joe McGill <joemcgill@gmail.com>
Date:   Wed Nov 16 23:26:31 2016 +0000

    Themes: Improve a11y and extendability of custom video headers.

    This adds play/pause controls to video headers, along with voice
    assistance, using `wp.a11y.speak`, to make custom video headers more
    accessible. To make styling the play/pause button easier for themes,
    CSS has been omitted from the default implementation.

    This also includes a refactor of the `wp.customHeader` code to introduce
    a `BaseHandler` class, which can be extended by plugins and themes to modify
    or enhance the default video handlers.

    Props davidakennedy, afercia, bradyvercher, joemcgill, adamsilverstein, rianrietveld.
    Fixes #38678.
    Built from https://develop.svn.wordpress.org/trunk@39272


    git-svn-id: http://core.svn.wordpress.org/trunk@39212 1a063a9b-81f0-0310-95a4-ce76da25c4cd