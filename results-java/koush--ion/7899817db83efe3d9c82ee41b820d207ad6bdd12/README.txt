commit 7899817db83efe3d9c82ee41b820d207ad6bdd12
Author: Koushik Dutta <koushd@gmail.com>
Date:   Tue Mar 18 22:16:17 2014 -0700

    Loader: refactor loadBitmap to accept a context.
    Cookies: Fix multiple cookie persistence.
    Bitmap loader: watch for improper usage of resize/deepZoom/transform/center*.
    Bitmap loader: resize/center is always the first transform that happens.
    Ion: remove request resolver cruft.