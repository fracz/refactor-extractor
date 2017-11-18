commit b9f6fdbc9b02971c7ef99bd4ceec7a48f8c196c7
Author: Daniel Hugenroth <hugenroth@fb.com>
Date:   Tue May 23 19:17:38 2017 -0700

    Bitmap.prepareToDraw: Add BitmapPrepareProducer

    Summary:
    This change adds a `BitmapPrepareProducer` that optimistically uploads a `CloseableStaticBitmap` to the GPU. Requests that are pre-fetching are ignored and just passed through.

    By calling `Bitmap#prepareToDraw` the `RenderThread` can upload the `Bitmap` asynchronously when it's idle. This has often moves the upload out of `DrawFrame` and therefore improves the critical path.

    This came up from issue https://github.com/facebook/fresco/issues/1756 that was risen from the Android UI Toolkit Graphics team.

    I've verified the change using `systrace` on my Google Pixel phone. Screenshots in the comments

    Reviewed By: erikandre

    Differential Revision: D5106592

    fbshipit-source-id: b2c9873c7b120f8bf4ab56c1eac9819c787b0757