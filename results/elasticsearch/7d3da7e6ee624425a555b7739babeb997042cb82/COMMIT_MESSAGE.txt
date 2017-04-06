commit 7d3da7e6ee624425a555b7739babeb997042cb82
Author: Ryan Ernst <ryan@iernst.net>
Date:   Sun Nov 22 14:46:47 2015 -0800

    Build: Get multi node smoke tests working

    This change adds back the multi node smoke test, as well as making the
    cluster formation for any test allow multiple nodes. The main changes in
    cluster formation are abstracting out the node specific configuration to
    a helper struct, as well as making a single wait task that waits for all
    nodes after their start tasks have run. The output on failure was also
    improved to log which node's info is being printed.