commit f069780bfa770ee497c5994ff276bf9eb1416aa5
Author: David Huynh <dfhuynh@gmail.com>
Date:   Sat Jul 31 06:51:11 2010 +0000

    Added support for bundling .js files to shave off some loading time.
    For GetRowsCommand, tried to use jsonp but that didn't seem to improve performance much.
    Gzip http responses of various text-based mime types.

    git-svn-id: http://google-refine.googlecode.com/svn/trunk@1122 7d457c2a-affb-35e4-300a-418c747d4874