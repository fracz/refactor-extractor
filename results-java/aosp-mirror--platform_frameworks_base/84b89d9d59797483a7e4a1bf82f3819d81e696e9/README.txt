commit 84b89d9d59797483a7e4a1bf82f3819d81e696e9
Author: Lucas Dupin <dupin@google.com>
Date:   Tue May 9 12:16:19 2017 -0700

    WallpaperColors refactor

    Hiding color extraction into WallpaperColors.
    This enables us to create WallpaperColors from a a Bitmap
    or Drawable.

    Fixes: 62197187
    Fixes: 62490115
    Test: runtest --path cts/tests/app/src/android/app/cts/WallpaperColorsTest.java
    Change-Id: I614cfa205e02b551a141642eac6de21251c3bff6