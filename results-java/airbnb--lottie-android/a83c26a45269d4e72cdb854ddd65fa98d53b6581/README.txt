commit a83c26a45269d4e72cdb854ddd65fa98d53b6581
Author: Gabriel Peal <gpeal@users.noreply.github.com>
Date:   Tue Feb 28 09:43:16 2017 -0800

    More masks and matte improvements (#169)

    Uses multiple levels of offscreen buffers instead of a bitmap to further improve mask and matte performance. There are now no bitmaps created for masks or mattes! Performance is comparable but memory is greatly reduced.

    Multiple masks were broken because they were applied sequentially rather than together. Fixes #161.