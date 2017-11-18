commit d2b300f8d1aff1b58b2353b369b079741c8fc379
Author: jasexton <jasexton@google.com>
Date:   Tue Sep 6 09:48:01 2016 -0700

    Avoid duplication of code in ImmutableValueGraph. Surprised I didn't realize this sooner :-/ the backing graph is a ValueGraph, so we can just call through. This also let's us do some other nice things:

    1) Get rid of the static methods in AbstractValueGraph.
    2) Get rid of the static method in AbstractGraph. That was actually for a different reason (because of ForwardingObject), but the only thing ForwardingObject did for us was forward the toString() method which we actually *don't* want.
    3) Allow us to "cache" the edge value map, and have both the mutable and immutable implementations inherit that behavior. If someone is calling e.g. graph.edgeValues().get(...) in a loop this should be a noticeable improvement.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=132330742