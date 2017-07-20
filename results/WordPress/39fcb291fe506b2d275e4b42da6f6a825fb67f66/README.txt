commit 39fcb291fe506b2d275e4b42da6f6a825fb67f66
Author: Andrew Nacin <wp@andrewnacin.com>
Date:   Wed Mar 5 06:26:14 2014 +0000

    Always convert auto-drafts to drafts in edit_draft() and _wp_translate_postdata().

    This regressed due to refactoring in [26995]. This commit fixes Quick Draft.

    see #25272.

    Built from https://develop.svn.wordpress.org/trunk@27405


    git-svn-id: http://core.svn.wordpress.org/trunk@27252 1a063a9b-81f0-0310-95a4-ce76da25c4cd