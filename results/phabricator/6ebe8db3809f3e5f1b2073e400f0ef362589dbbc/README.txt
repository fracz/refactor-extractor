commit 6ebe8db3809f3e5f1b2073e400f0ef362589dbbc
Author: epriestley <git@epriestley.com>
Date:   Thu Jan 21 10:18:00 2016 -0800

    Add a missing key to HarbormasterBuildArtifact

    Summary: Fixes T10192. This key improves some common queries and is not currently present.

    Test Plan: See discussion in T10192. Verified current query plan of real queries is garbage and improved by adding this key.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10192

    Differential Revision: https://secure.phabricator.com/D15075