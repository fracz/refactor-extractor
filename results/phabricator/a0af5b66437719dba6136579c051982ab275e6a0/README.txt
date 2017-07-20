commit a0af5b66437719dba6136579c051982ab275e6a0
Author: epriestley <git@epriestley.com>
Date:   Fri May 6 07:18:09 2011 -0700

    Make DifferentialChangesetParser explicitly map display to storage for comments

    Summary:
    This is a followup to D228. Basically, we use "changeset" (and, implicitly,
    changesetID) for way too much stuff now.

    One thing it can't possibly capture is the complete, arbitrary mapping between
    the left and right sides of the displayed diff and the places we want to store
    the left and right side comments. This causes a bunch of bugs; basically adding
    inline comments is completely broken in diff-of-diff views prior to this patch.
    Make this mapping explicit.

    Note that the renderer already passes this mapping to
    DifferentialChangesetParser which is why there are no changes outside this file,
    I just didn't finish the implementation during the port.

    This has the nice side-effect of fixing T132 and several other bugs.

    Test Plan:
    Made new-file and old-file comments on a normal diff; reloaded page, verified
    comments didn't do anything crazy.

    Expanded text on a normal diff, made new-file and old-file comments; reloaded
    page, verified comments.

    Repeated these steps for a previous diff in the same revision; verified
    comments.

    Loaded diff-of-diffs and verified expected comments appeared. Made new left and
    right hand side comments, which almost work, see below.

    NOTE: There is still a bug where comments made in the left-display-side of a
    diff-of-diffs will incorrectly be written to the right-storage-side of the
    right-display-side diff. However, this is an issue with the JS (the PHP is
    correct) so I want to pull it out of scope for this patch since I think I need
    to fix some other JS stuff too and this improves the overall state of the world
    even if not everything is fixed.

    Reviewed By: tuomaspelkonen
    Reviewers: jungejason, tuomaspelkonen, aran, ola
    CC: aran, tuomaspelkonen, epriestley
    Differential Revision: 237