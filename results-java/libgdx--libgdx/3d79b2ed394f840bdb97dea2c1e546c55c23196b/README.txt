commit 3d79b2ed394f840bdb97dea2c1e546c55c23196b
Author: Manuel Bua <manuel.bua@gmail.com>
Date:   Sun May 5 00:27:39 2013 +0200

    Packing tilesets should walk all used tilesets, not only the last one

    Previously the last loaded Map object was used to walk the tilesets and
    pack them, now tilesets are being tracked consistently and enumerated
    correctly.

    Minor refactoring.

    Add support for packing and loading multiple atlases in a single map,
    adding a custom map property "atlas_<tilesetname>" to link a tileset to its
    atlas.