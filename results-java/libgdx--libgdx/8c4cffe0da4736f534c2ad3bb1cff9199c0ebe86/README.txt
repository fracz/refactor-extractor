commit 8c4cffe0da4736f534c2ad3bb1cff9199c0ebe86
Author: Manuel Bua <manuel.bua@gmail.com>
Date:   Sun May 5 00:34:33 2013 +0200

    Add new Tiled map loader with TextureAtlas support

    Initial code from Justin Shapcott, thanks for sharing!

    Port all changes from latest TmxMapLoader, load more map properties, check
    for correct encoding
    Minor refactoring
    Add support for force-enabling custom texture filters
    Add support for multiple TextureAtlases