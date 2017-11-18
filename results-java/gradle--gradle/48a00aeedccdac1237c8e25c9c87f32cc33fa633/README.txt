commit 48a00aeedccdac1237c8e25c9c87f32cc33fa633
Author: Adam Murdoch <adam@gradle.com>
Date:   Sun Jan 22 10:44:58 2017 +1000

    Extracted a service that takes care of cross-build in-memory caching, moving this logic out of `InMemoryTaskArtifactCache` so that it can be reused elsewhere.

    This infrastructure currently uses an intentionally simple strategy, retaining strong references to values using in the current build session and previous build session, and soft references to all other values. This can be improved later, particularly now that this is a service. For example, this service might receive an event from the JVM health monitoring infrastructure and release strong references in reaction to low heap.