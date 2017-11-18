commit 8cc6a5026aeb5cf9cc36529426fe0cc66714f5fb
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Aug 5 21:29:42 2009 -0700

    First bit of wallpaper work.

    This is mostly refactoring, adding a new WallpaperManager class that takes care
    of the old wallpaper APIs on Context, so we don't need to pollute Context with
    various new wallpaper APIs as they are needed.  Also adds the first little
    definition of a wallpaper service, which is not yet used or useful.