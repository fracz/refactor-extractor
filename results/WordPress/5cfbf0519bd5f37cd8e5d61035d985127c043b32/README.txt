commit 5cfbf0519bd5f37cd8e5d61035d985127c043b32
Author: Adam Silverstein <adamsilverstein@earthboundhosting.com>
Date:   Mon Jul 10 19:08:41 2017 +0000

    Media: library grid view - improve browser history support.

    Set view state properly when navigating history using the browser back/next button in the media library (grid view). Correctly handle navigating, search, image detail view and image edit mode. Also handle bookmarking/reloading.

    Props kucrut, joemcgill, afercia.
    Fixes #31846.


    Built from https://develop.svn.wordpress.org/trunk@41021


    git-svn-id: http://core.svn.wordpress.org/trunk@40871 1a063a9b-81f0-0310-95a4-ce76da25c4cd