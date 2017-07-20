commit b0a5f42244b4ce05ff159c715e7416104a884906
Author: epriestley <git@epriestley.com>
Date:   Mon May 20 10:18:26 2013 -0700

    Add "live" publisher and storage to Diviner

    Summary:
    Ref T988. This adds basics for the non-static publishing target:

      - Storage (called "Live", e.g. `DivinerLiveAtom` to distinguish it from shared classes like `DivinerAtom`).
      - Mostly populate the storage.
      - Some minor fixes and improvements.

    Test Plan: Generated docs, looked at DB, saw mostly-sensible output.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T988

    Differential Revision: https://secure.phabricator.com/D5973