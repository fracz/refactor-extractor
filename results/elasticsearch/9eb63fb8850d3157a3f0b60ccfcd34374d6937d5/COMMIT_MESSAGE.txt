commit 9eb63fb8850d3157a3f0b60ccfcd34374d6937d5
Author: Nicholas Knize <nknize@gmail.com>
Date:   Wed Aug 24 14:14:11 2016 -0500

    Refactor GeoPointFieldMapperLegacy and Legacy BBox query helpers

    This is a house cleaning commit that refactors GeoPointFieldMapperLegacy to LegacyGeoPointFieldMapper for consistency with Legacy Numerics and IP field mappers.

    IndexedGeoBoundingBoxQuery and InMemoryGeoBoundingBoxQuery are also deprecated and refactored as Legacy classes.