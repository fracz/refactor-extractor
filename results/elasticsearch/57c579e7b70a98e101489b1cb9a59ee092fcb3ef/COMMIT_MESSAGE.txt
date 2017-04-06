commit 57c579e7b70a98e101489b1cb9a59ee092fcb3ef
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed Dec 16 12:21:17 2015 +0100

    Geo: Add validation of shapes to shape builders

    So far the validation of geo shapes was only taking place in the
    parse methods in ShapeBuilder. With the recent refactoring we no
    longer can rely on shapes being parsed from json, so the same kind
    of validation should take place when just using the java api.

    A lot of validation concerns the number of points a shape needs to
    have in order to be valid. Since this is not possible with current
    builders where points can be added one by one, the builder constructors
    are changed to require the mandatory parameters and validate those
    already at construction time. To help with constructing longer lists
    of points, a new utility PointsListBuilder is instroduces which can
    produce list of coordinates accepted by most of the other shape builder
    constructors.

    Also adding tests for invalid shape exceptions to the already existing
    shape builder tests.