commit 31058a094a50055898692f6930b47b18a4027b4f
Author: Steve Clay <steve@mrclay.org>
Date:   Thu Dec 11 19:38:05 2014 -0500

    perf(entities): loads more entities with a single query

    This optimizes elgg_get_entities() to automatically join secondary tables
    to load entities in fewer queries. It also improves get_entity() by using
    elgg_get_entities internally. This also improves river prefetching by
    taking advantage of this optimization.

    Fixes #7662, #7659