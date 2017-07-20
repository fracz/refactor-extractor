commit e58856bbd081d1af2ad96950f83590b3f6d06c47
Author: David A. Kennedy <me@davidakennedy.com>
Date:   Fri Oct 21 22:15:50 2016 +0000

    Twenty Seventeen: Display featured image on static front page

    This improves UX, since an image added will be displayed on front end as opposed to not at all. This fix added the front page's featured image above the front page content, similar to how it's handled in the other panels. Also it removed code that was setting the front page's featured image as a fallback to the custom header, and updated the conditions that add the `has-header-image` to remove reference to the front page's featured image.

    Props laurelfulford.

    Fixes #38402.

    Built from https://develop.svn.wordpress.org/trunk@38868


    git-svn-id: http://core.svn.wordpress.org/trunk@38811 1a063a9b-81f0-0310-95a4-ce76da25c4cd