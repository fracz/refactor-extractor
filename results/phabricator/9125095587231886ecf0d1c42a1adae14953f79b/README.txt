commit 9125095587231886ecf0d1c42a1adae14953f79b
Author: epriestley <git@epriestley.com>
Date:   Sat Oct 26 19:16:10 2013 -0700

    Distinguish between empty and unparsed commits in Diffusion

    Summary:
    Fixes T3416. Fixes T1733.

      - Adds a flag to the commit table showing whether or not we have parsed it.
      - The flag is set to `0` initially when the commit is discovered.
      - The flag is set to `1` when the changes are parsed.
      - The UI can now use the flag to distinguish between "empty commit" and "commit which we haven't imported changes for yet".
      - Simplify rendering code a little bit.
      - Fix an issue with the Message parser for empty commits.
      - There's a key on the flag so we can do `SELECT * FROM repository_commit WHERE repositoryID = %d AND importStatus = 0 LIMIT 1` soon, to determine if a repository is fully imported or not. This will let us improve the UI (Ref T776, Ref T3217).

    Test Plan:
      - Ran `bin/storage upgrade -f`.
      - Created an empty commit.
      - Without the daemons running, ran `bin/repository pull GTEST` and `bin/repository discover GTEST`.
      - Viewed web UI to get the first screenshot ("Still Importing...").
      - Ran the message and change steps with `scripts/repository/reparse.php`.
      - Viewed web UI to get the second screenshot ("Empty Commit").

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T776, T1733, T3416, T3217

    Differential Revision: https://secure.phabricator.com/D7428