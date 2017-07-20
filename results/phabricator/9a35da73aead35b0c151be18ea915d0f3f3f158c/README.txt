commit 9a35da73aead35b0c151be18ea915d0f3f3f158c
Author: epriestley <git@epriestley.com>
Date:   Tue Jul 23 12:10:30 2013 -0700

    Use modern UI elements to show daemons in Daemons

    Summary:
    Ref T3557. Slightly improves display of daemons:

      - Makes status more clear (through colors, explanatory text, icons, and explicit descriptions instead of symbols).
      - Particularly, the "wait" status is now communicated as a normal status ("waiting a moment...") with a calm blue color.
      - Uses modern responsive elements.

    Test Plan: {F51232}

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T3557

    Differential Revision: https://secure.phabricator.com/D6539