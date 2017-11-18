commit a94fb92ac5936e137cee028fc96d73bd7dc3a03d
Author: Adrien Grand <jpountz@gmail.com>
Date:   Mon Nov 17 18:43:22 2014 +0100

    Aggregations: Fix geohash grid aggregation on multi-valued fields.

    This aggregation creates an anonymous fielddata instance that takes geo points
    and turns them into a geo hash encoded as a long. A bug was introduced in 1.4
    because of a fielddata refactoring: the fielddata instance tries to populate
    an array with values without first making sure that it is large enough.

    Close #8507