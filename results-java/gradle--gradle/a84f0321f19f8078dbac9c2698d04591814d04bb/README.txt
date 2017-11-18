commit a84f0321f19f8078dbac9c2698d04591814d04bb
Author: Cedric Champeau <cedric@gradle.com>
Date:   Tue Feb 14 18:58:08 2017 +0100

    Introduce an API to supply component metadata for dynamic versions in Ivy repositories

    This commits adds a new API, available on Ivy repositories only, aimed at allowing the
    user to provide component metadata in place of relying on Ivy XML metadata parsing.
    The goal of this approach is to support custom metadata rules in the long term. In
    particular, the rule is instantiated by Gradle, for a resolution scope, and can cache
    results. This means that a single remote call can be done to get the list of all versions
    of a module, typically, and the metadata rule can set the `status` of the component
    without having to parse the descriptor. Since the status is the only information that
    is required (with the scheme, which is in practice constant), this means that only
    nodes that will effectively be added to the dependency graph will have their Ivy
    descriptor parsed. By caching the remove call, we will be able to reduce the amount of
    calls made to the remote server to get this information, so we're more effective in 2
    areas :

    - parsing metadata, which is limited to nodes included in the graph instead of all nodes
    until we find one matching the appropriate status
    - limiting remote calls, since the rule can have a cache (discarded after resolution)

    It's worth noting that the scope of the rule is the same as the `IvyResolver`. This could
    be improved. Similarly, we do _not_ provide a way to perform cached remote calls yet. This
    is going to be introduced in subsequent commits.