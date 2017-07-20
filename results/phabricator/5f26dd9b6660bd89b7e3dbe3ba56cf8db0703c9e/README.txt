commit 5f26dd9b6660bd89b7e3dbe3ba56cf8db0703c9e
Author: epriestley <git@epriestley.com>
Date:   Thu Dec 8 06:39:33 2016 -0800

    Use futures to improve clustered repository main page performance

    Summary:
    Ref T11954. In cluster configurations, we get repository information by making HTTP calls over Conduit.

    These are slower than local calls, so clustering imposes a performance penalty. However, we can use futures and parallelize them so that clustering actually improves overall performance.

    When not running in clustered mode, this just makes us run stuff inline.

    Test Plan:
      - Browsed Git, Mercurial and Subversion repositories.
      - Locally, saw a 700ms wall time page drop to 200ms.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11954

    Differential Revision: https://secure.phabricator.com/D17009