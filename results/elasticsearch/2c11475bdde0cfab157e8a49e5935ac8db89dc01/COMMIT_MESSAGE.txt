commit 2c11475bdde0cfab157e8a49e5935ac8db89dc01
Author: pickypg <pickypg@gmail.com>
Date:   Tue May 6 14:40:46 2014 +0200

    Update geo-shape-type documentation

    Update `geo-shape-type.asciidoc` to include all `GeoShapeType`s supported by the `org.elasticsearch.common.geo.builders.ShapeBuilder`.

    Changes include:

    1. A tabular mapping of GeoJSON types to Elasticsearch types
    2. Listing all types, with brief examples, for all support Elasticsearch types
    3. Putting non-standard types to the bottom (really just moving Envelope to the bottom)
    4. Linking to all GeoJSON types.
    5. Adding whitespace around tightly nested arrays (particularly `multipolygon`) for readability