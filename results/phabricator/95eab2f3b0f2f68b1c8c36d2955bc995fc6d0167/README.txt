commit 95eab2f3b0f2f68b1c8c36d2955bc995fc6d0167
Author: epriestley <git@epriestley.com>
Date:   Sat May 10 12:41:18 2014 -0700

    Record parent relationships when discovering commits

    Summary:
    Ref T4455. This adds a `repository_parents` table which stores `<childCommitID, parentCommitID>` relationships.

    For new commits, it is populated when commits are discovered.

    For older commits, there's a `bin/repository parents` script to rebuild the data.

    Right now, there's no UI suggestion that you should run the script. I haven't come up with a super clean way to do this, and this table will only improve performance for now, so it's not important that we get everyone to run the script right away. I'm just leaving it for the moment, and we can figure out how to tell admins to run it later.

    The ultimate goal is to solve T2683, but solving T4455 gets us some stuff anyway (for example, we can serve `diffusion.commitparentsquery` faster out of this cache).

    Test Plan:
      - Used `bin/repository discover` to discover new commits in Git, SVN and Mercurial repositories.
      - Used `bin/repository parents` to rebuild Git and Mercurial repositories (SVN repos just exit with a message).
      - Verified that the table appears to be sensible.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: jhurwitz, epriestley

    Maniphest Tasks: T4455

    Differential Revision: https://secure.phabricator.com/D9044