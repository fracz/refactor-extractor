commit 7ca3f066f46d29d69c9b2bc24dbf0d12967b7e88
Author: epriestley <git@epriestley.com>
Date:   Tue Aug 27 03:14:00 2013 -0700

    Generate PHP function documentation in Diviner

    Summary:
    Ref T988. Various improvements:

      - Generate function documentation, mostly correctly.
      - Raise some warnings about bad documentation.
      - Allow `.book` files to exclude paths from generation.
      - Add a book for technical docs.
      - Exclude "ghosts" from common queries (atoms which used to exist, but no longer do, but which we want to keep the PHIDs around for in case they come back later).

    This is a bit rough still, but puts us much closer to being able to get rid of the old Diviner.

    Test Plan: See screenshots.

    Reviewers: btrahan, chad

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T988

    Differential Revision: https://secure.phabricator.com/D6812