commit 5cc6c39430c9a0dccc47d14eebf47e8400cebb5d
Author: Adam Silverstein <adamsilverstein@earthboundhosting.com>
Date:   Tue May 23 20:32:44 2017 +0000

    Themes: improve browser history support on new themes page.

    When closing the theme preview, restore the previously selected tab. Avoid an issue where duplicate entries in the history prevented navigation. When re-opening the preview, remove bound event handlers before re-adding them.

    Props afercia.
    Fixes #36613.

    Built from https://develop.svn.wordpress.org/trunk@40824


    git-svn-id: http://core.svn.wordpress.org/trunk@40681 1a063a9b-81f0-0310-95a4-ce76da25c4cd