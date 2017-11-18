commit b91e03b91cd560703e9137b1dd07a5521b8919e1
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Mon Apr 10 12:23:54 2017 +0200

    Fix devtools support in NettyWebServer

    This commit improves the lifecycle of the `NettyWebServer` and allows
    multiple restarts when using devtools. Previously, the lifecycle was
    tailored for a single start/stop cycle.

    Fixes gh-8771