commit 40787f29c356b4d550e7e310d89aa8e5289feaba
Author: Jochen Schalanda <jochen@torch.sh>
Date:   Fri Aug 22 19:54:34 2014 +0200

    Try to speed up GracefulShutdown and to fail fast

    There were some possibilities in `GracefulShutdown` to speed up a graceful
    shutdown of the system. Instead of waiting for an Elasticsearch cluster to
    reappear we now fail fast (because there is no use in trying to flush buffered
    messages to a non-existing Elasticsearch cluster).

    Additionally `ServerStatus` has been refactored and now offers distinct methods
    for lifecycle changes instead of an opaque (and tainted with side-effects)
    `setLifecycle` method.

    Fixes #480