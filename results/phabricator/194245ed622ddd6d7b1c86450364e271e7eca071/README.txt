commit 194245ed622ddd6d7b1c86450364e271e7eca071
Author: epriestley <git@epriestley.com>
Date:   Sun Sep 8 09:16:55 2013 -0700

    Clean up some more Diviner stuff

    Summary:
    Ref T988.

      - Render "Implements:" as tags, too.
      - Minor CSS tweak to tags in property lists.
      - Add a bunch of group patterns to the Phabricator book.
      - Fix some stuff with how hashes are computed and cached.
      - Minor tweak to reuse the Diviner engine for slightly improved performance.

    Test Plan: Regenerated and looked at documentation.

    Reviewers: chad

    Reviewed By: chad

    CC: aran

    Maniphest Tasks: T3811, T988

    Differential Revision: https://secure.phabricator.com/D6912