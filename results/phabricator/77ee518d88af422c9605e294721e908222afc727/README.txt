commit 77ee518d88af422c9605e294721e908222afc727
Author: epriestley <git@epriestley.com>
Date:   Thu Jun 16 07:50:23 2016 -0700

    Make daemons ignore "Unreachable" commits and avoid duplicate work

    Summary:
    Ref T9028. This improves the daemon behavior for unreachable commits. There is still no way for commits to become marked unreachable on their own.

      - When a daemon encounters an unreachable commit, fail permanently.
      - When we revive a commit, queue new daemons to process it (since some of the daemons might have failed permanently the first time around).
      - Before doing a step on a commit, check if the step has already been done and skip it if it has. This can't happen normally, but will soon be possible if a commit is repeatedly deleted and revived very quickly.
      - Steps queued with `bin/repository reparse ...` still execute normally.

    Test Plan:
      - Used `bin/repository reparse` to run every step, verified they all mark the commit with the proper flag.
      - Faked the `reparse` exception in the "skip step" code, used `repository reparse` to skip every step.
      - Marked a commit as unreachable, ran `discover`, saw daemons queue for it.
      - Ran daemons with `bin/worker execute --id ...`, saw them all skip + queue the next step.
      - Marked a commit as unreachable, ran `bin/repository reparse` on it, got permanent failures immediately for each step.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T9028

    Differential Revision: https://secure.phabricator.com/D16131