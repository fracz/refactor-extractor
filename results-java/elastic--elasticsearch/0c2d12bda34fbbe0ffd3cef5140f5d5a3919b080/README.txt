commit 0c2d12bda34fbbe0ffd3cef5140f5d5a3919b080
Author: Florian Schilling <schilling@flonkings.com>
Date:   Thu May 23 10:23:39 2013 +0200

    Geo-Refactoring
    ===============
    The code handling geo-shapes is not centralized and creating points takes
    place at different places. Also the collection of supported geo_shapes is
    not complete regarding to the GEOJSon specification. This commit
    centralizes the code related to GEO calculations and extends the old API by
    a set of new shapes.

    Null-Shapes
    ===========
    The latest implementation of geo-shapes allows to index null-shapes. This
    means a field that is defined to hold a geo-shape can be set to null. In
    example:
        {

            "shape": null
        }

    New Shapes
    ==========
    The geo-shapes multipoint and multilinestring have been added to the
    geo_shape types. Also geo_circle is introduced by this commit.

    Dateline wrapping
    =================
    A major issue of geo-shapes is the spherical geometry. Since ElasticSearch
    works on the Geo-Coordinates by wrapping the Earths surface to a plane,
    some shapes are hard to define if it’s crossing the +180°, -180 longitude.
    To solve this issue ElasticSearch offers the possibility to define geo
    shapes crossing this borders and decompose these shapes and automatically
    re-compose them in a spherical manner. This feature may change the indexed
    shape-type. If for example a polygon is defined, that crosses the dateline,
    it will be re-assembled to a set of polygons. This causes indexing a
    multipolygon. Also linestrings crossing the dateline might be re-assembled
    to multilinestrings.

    Builders
    ========
    The API has been refactored to use builders instead of using shapes. So
    parsing geo-shapes will result in builder objects. These builders can be
    parsed and serialized without generating any shapes. this causes shape
    generation only on the nodes executing the actual operation. Also the
    baseclass ShapeBuilder implements the ToXContent interface which allows to
    set fields of XContent directly.

    TODO’s
    ======
     - The geo-circle will not work, if it’s crossing the dateline
     - The envelope also needs to wrapped

    Closes #1997 #2708