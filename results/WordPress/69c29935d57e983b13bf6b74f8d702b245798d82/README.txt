commit 69c29935d57e983b13bf6b74f8d702b245798d82
Author: Andrew Ozz <admin@laptoptips.ca>
Date:   Fri Jan 27 04:25:44 2017 +0000

    TinyMCE: improve the previews for embedded WordPress posts:
    - Add option to force a sandbox iframe in wpview.
    - Use it to show the embedded post preview.
    - Remove the deprecated `wpembed` plugin.js

    Fixes #39513.
    Built from https://develop.svn.wordpress.org/trunk@40019


    git-svn-id: http://core.svn.wordpress.org/trunk@39956 1a063a9b-81f0-0310-95a4-ce76da25c4cd