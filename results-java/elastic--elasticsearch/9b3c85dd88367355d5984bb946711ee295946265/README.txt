commit 9b3c85dd88367355d5984bb946711ee295946265
Author: Jim Ferenczi <jim.ferenczi@elastic.co>
Date:   Mon Apr 10 10:10:16 2017 +0200

    Deprecate _field_stats endpoint (#23914)

    _field_stats has evolved quite a lot to become a multi purpose API capable of retrieving the field capabilities and the min/max value for a field.
    In the mean time a more focused API called `_field_caps` has been added, this enpoint is a good replacement for _field_stats since he can
    retrieve the field capabilities by just looking at the field mapping (no lookup in the index structures).
    Also the recent improvement made to range queries makes the _field_stats API obsolete since this queries are now rewritten per shard based on the min/max found for the field.
    This means that a range query that does not match any document in a shard can return quickly and can be cached efficiently.
    For these reasons this change deprecates _field_stats. The deprecation should happen in 5.4 but we won't remove this API in 6.x yet which is why
     this PR is made directly to 6.0.
     The rest tests have also been adapted to not throw an error while this change is backported to 5.4.