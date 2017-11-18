commit 9eff185e73124bc39f44e0f6f40043d39ba34277
Author: Karol Wrótniak <koral--@users.noreply.github.com>
Date:   Mon May 23 03:50:56 2016 +0200

    - Fixed erroneous `GifDrawableBuilder#options()` argument modification after calling `GifDrawableBuilder#sampleSize()`
    - Javadoc improvements
    - Added passing opacity hint from `GifOptions` to `Bitmap` (framebuffer) in `GifDrawable`