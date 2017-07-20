commit 0a7973488f5e69779d9d227a50361826aa614660
Author: vrana <jakubv@fb.com>
Date:   Wed Jun 27 10:44:29 2012 -0700

    Simplify DifferentialHunk::getAddedLines()

    Summary: I will also need `getRemovedLines()` so refactor this first.

    Test Plan:
    New test case.
    Viewed uncached diff.
    Verified that the only callsite of `getAddedLines()` trims lines.

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: aran, Korvin

    Differential Revision: https://secure.phabricator.com/D2875