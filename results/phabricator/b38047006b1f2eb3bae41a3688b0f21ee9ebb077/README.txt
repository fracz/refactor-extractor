commit b38047006b1f2eb3bae41a3688b0f21ee9ebb077
Author: epriestley <git@epriestley.com>
Date:   Fri Mar 30 10:13:08 2012 -0700

    Show other open revisions affecting the same files in Differential

    Summary:
    I think this feature is probably good, but Differential is also really starting to get a lot of stuff which is not the diff in it. Not sure how best to deal with that.

    The mixed table styles are also pretty ugly.

    So I guess this is more feedback / proof-of-concept, I think I want to try to improve it somehow before I land it.

    Test Plan: Looked at some diffs, some had an awkward, ugly list of diffs affecting the same files.

    Reviewers: bill, aran, btrahan

    Reviewed By: aran

    CC: aran, epriestley

    Maniphest Tasks: T829

    Differential Revision: https://secure.phabricator.com/D2027