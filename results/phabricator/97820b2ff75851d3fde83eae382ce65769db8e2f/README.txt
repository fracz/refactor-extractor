commit 97820b2ff75851d3fde83eae382ce65769db8e2f
Author: epriestley <git@epriestley.com>
Date:   Tue Jan 24 08:31:45 2012 -0800

    Add branch queries to differential.query

    Summary: This enables some improvements in D1478. Allow revisons to be queried
    by the branch which they appear on.

    Test Plan: Queried revisions by branch. Ran "arc which" branch queries in SVN
    and Mercurial.

    Reviewers: btrahan, cpiro, jungejason

    Reviewed By: btrahan

    CC: aran, epriestley

    Maniphest Tasks: T787

    Differential Revision: https://secure.phabricator.com/D1479