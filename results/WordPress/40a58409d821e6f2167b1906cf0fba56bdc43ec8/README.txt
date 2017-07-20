commit 40a58409d821e6f2167b1906cf0fba56bdc43ec8
Author: Sergey Biryukov <sergeybiryukov.ru@gmail.com>
Date:   Tue Nov 3 20:49:27 2015 +0000

    Accessibility: After [35130], adjust the `h2.nav-tab-wrapper` selector to only override `.wrap > h2:first-child`.

    This improves compatibility with plugins that use navigation tabs as a first H2 heading on their screens.

    Props afercia.
    Fixes #33559.
    Built from https://develop.svn.wordpress.org/trunk@35496


    git-svn-id: http://core.svn.wordpress.org/trunk@35460 1a063a9b-81f0-0310-95a4-ce76da25c4cd