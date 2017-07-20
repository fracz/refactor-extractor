commit 5cdb46c16a9b966d280857ed409c585ba14b4d2e
Author: Mike Schroder <mschrode@umich.edu>
Date:   Wed Nov 9 21:05:30 2016 +0000

    Media: Only load first PDF page for thumbnails.

    To improve performance, directly load the first page of uploaded PDFs
    to reduce the total clock time necessary to generate thumbnails.

    Props dglingren, lukecavanagh, helen, johnbillion, mikeschroder.
    See #38522.
    Built from https://develop.svn.wordpress.org/trunk@39187


    git-svn-id: http://core.svn.wordpress.org/trunk@39127 1a063a9b-81f0-0310-95a4-ce76da25c4cd