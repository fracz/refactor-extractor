commit 74d641181e77beb4bc350493bab6f69872665691
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Sep 22 14:44:41 2015 +0200

    Query Refactoring: validate GeoShapeQueryBuilder strategy and relation parameter

    Before the refactoring we didn't check any invalid settings for strategy and relation
    in the GeoShapeQueryBuilder. However, using SpatialStrategy.TERM and ShapeRelation.INTERSECTS
    together is invalid and we tried to protect against that in the validate() method.

    This PR moves these checks to setter for strategy and relation and adds tests for the new
    behaviour.

    Relates to #10217