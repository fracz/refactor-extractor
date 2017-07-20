commit 0a49442e3532b068ebab495682697bef4264d19d
Author: Mark Jaquith <mark@wordpress.org>
Date:   Mon Apr 22 22:11:42 2013 +0000

    Multiple improvements to image post format insertion and display.

    * get_tag_regex() altered based on Unit Tests.
    * Changes to post-formats.js to provide size and link context during image selection.
    * Captions are now output in the_post_format_image() when present.
    * The meta value for url is respected for the image post format when the HTML in the image meta doesn't include a link

    props wonderboymusic. fixes #23965, #23964. see #24147, #24046.

    git-svn-id: http://core.svn.wordpress.org/trunk@24066 1a063a9b-81f0-0310-95a4-ce76da25c4cd