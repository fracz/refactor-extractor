commit f36c852542e91f2c2e9406b28867372e5093cd0b
Author: epriestley <git@epriestley.com>
Date:   Wed May 11 05:11:59 2011 -0700

    Issue a warning if `javelinsymbols` is not built

    Summary:
    Currently, the Javelin linter fails completley if this binary is missing.
    However, it's hard to build and not critical so just issue a warning.

    Eventually we can document this better and make the build easier, but the
    current behavior is pretty unfriendly so make it smoother until the state of the
    world can be improved.

    Test Plan:
    Removed the binary and ran "arc lint --lintall" against multiple Javelin paths.
    Received one warning. Restored the binary and ran with "--trace", got no
    warnings and verified that the binary was running.

    Reviewed By: jungejason
    Reviewers: tuomaspelkonen, jungejason, aran, tomo
    CC: aran, jungejason
    Differential Revision: 265