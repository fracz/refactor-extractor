commit 12941bd4b745cb81a3d82264a0152200219885b0
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Nov 3 17:24:44 2015 +0100

    Geo: remove InternalLineStringBuilder

    This is a first step in reducing the number of ShapeBuilders since
    before we start making the remaining implement Writable for the
    search request refactoring. This shape builder seems to have been
    only used in tests, and those tests didn't do much to begin with,
    so this removed them.

    Relates to #14416