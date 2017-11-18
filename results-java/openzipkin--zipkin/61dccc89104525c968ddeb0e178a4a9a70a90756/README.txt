commit 61dccc89104525c968ddeb0e178a4a9a70a90756
Author: Adrian Cole <adriancole@users.noreply.github.com>
Date:   Wed Jul 26 21:07:58 2017 +0800

    Don't use strict ID when processing dependency links (in-memory) (#1665)

    The existing code inadvertently used strict trace IDs when looking up
    dependency link spans. This caused me a lot of head scratching when
    trying to figure out why later refactoring caused multiple links on a
    mixed trace.