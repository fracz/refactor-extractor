commit 2023711f17b5c616aa93d72df8986377948c8275
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Sat Aug 27 11:42:07 2016 +0200

    Bootstrap by downloading snapshots

    Previously RAFT would by convention match histories before index 0
    and thus the first entry would always be accepted. This commit however
    changes that so that the pre-history never matches effectively forcing
    a snapshot to be downloaded by all but the bootstrapper. The entry
    zero should always be bootstrapped in term 0 as a new convention. The
    log shipper should never try to ship anything less than 1.

    This work also discovered that membership state was not included in
    snapshots, which was wrong.

    Another aspect improved is the RPC error handling for various calls,
    removing some hotch-potches of exception/return value wrapping,
    unwrapping and whatnot. The proposed way of dealing with these
    issues is to rely on clearly defined result codes.

    Yet another thing looked at are the actual scenarios of what should
    happen in the cases of having empty stores, mismatched stores, etc.
    The guiding principle is that a an empty store (effectively no store)
    should be overwritten in its entirety (by downloading a snapshot) and
    that all other stores can be caught up where possible or a fresh
    snapshot downloaded if the store id matches. Where there is a store
    id mismatch, the server should panic and require operator
    intervention.

    This is also a first step towards not relying on store id for cluster
    identity. A test for not being able to start with the wrong store id
    is Ignored temporarily pending another design.