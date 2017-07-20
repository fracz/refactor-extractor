commit ac19c558225c5d94050f86677f8276b4960c0908
Author: epriestley <git@epriestley.com>
Date:   Thu Dec 26 10:40:43 2013 -0800

    Formalize "manual" buildables in Harbormaster

    Summary:
    Ref T1049. Generally, it's useful to separate test/trial/manual runs from production/automatic runs.

    For example, you don't want to email a bunch of people that the build is broken just because you messed something up when writing a new build plan. You'd rather try it first, then promote it into production once you have some good runs.

    Similarly, test runs generally should not affect the outside world, etc. Finally, some build steps (like "wait for other buildables") may want to behave differently when run in production/automation than when run in a testing environment (where they should probably continue immediately).

    So, formalize the distinction between automatic buildables (those created passively by the system in response to events) and manual buildables (those created explicitly by users). Add filtering, and stop the automated parts of the system from interacting with the manual parts (for example, we won't show manual results on revisions).

    This also moves the "Apply Build Plan" to a third, new home: instead of the sidebar or Buildables, it's now on plans. I think this generally makes more sense given how things have developed. Broadly, this improves isolation of test environments.

    Test Plan: Created some builds, browsed around, used filters, etc.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T1049

    Differential Revision: https://secure.phabricator.com/D7824