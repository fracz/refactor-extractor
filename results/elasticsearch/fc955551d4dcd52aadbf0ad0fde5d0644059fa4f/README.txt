commit fc955551d4dcd52aadbf0ad0fde5d0644059fa4f
Author: Nicholas Knize <nknize@gmail.com>
Date:   Mon Nov 17 16:23:03 2014 -0600

    [GEO] Fix for geo_shape query with polygon from -180/90 to 180/-90

    This fix adds a simple consistency check that intersection edges appear pairwise. Polygonal boundary tests were passing (false positive) on the Eastern side of the dateline simply due to the initial order (edge direction) of the intersection edges.  Polygons in the Eastern hemispehere (which were not being tested) were correctly failing inside of JTS due to an attempt to connect incorrect intersection edges (that is, edges that were not even intersections). While this patch fixes issue/8467 (and adds broader test coverage) it is not intented as a long term solution.  The mid term fix (in work) will refactor all geospatial computational geometry to use ENU / ECF coordinate systems for higher accuracy and eliminate brute force mercator checks and conversions.

    Closes #8467