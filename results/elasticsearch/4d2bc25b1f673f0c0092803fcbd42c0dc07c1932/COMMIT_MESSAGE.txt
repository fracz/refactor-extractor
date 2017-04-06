commit 4d2bc25b1f673f0c0092803fcbd42c0dc07c1932
Author: Michael McCandless <mail@mikemccandless.com>
Date:   Wed Apr 22 06:24:42 2015 -0400

    Make NodeEnvironment.getFileStore a bit more defensive

    This improves the NodeEnvironment code that walks through all mount
    points looking for the one matching the file store for a specified
    path, to make it a bit more defensive.  We currently rely on this to
    log the correct file system type of the path.data paths.

    Closes #10696