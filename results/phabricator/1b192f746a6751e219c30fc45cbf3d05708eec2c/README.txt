commit 1b192f746a6751e219c30fc45cbf3d05708eec2c
Author: epriestley <git@epriestley.com>
Date:   Sun Jul 31 09:41:17 2016 -0700

    Improve performance when constructing custom fields for objects

    Summary:
    Ref T11404. This improves things by about 10%:

      - Use `PhutilClassMapQuery`, which has slightly better caching.
      - Do a little less work to generate pretty error messages.
      - Make the "disabled" code a little faster (and sort of clearer, too?) by doing less fancy stuff.

    These are pretty minor adjustments and not the sort of optimizations I'd make normally, but this code gets called ~100x (once per revision) and generates ~10 fields normally, so even small savings can amount to something.

    (I also want to try to make `arc` faster in the next update, and improving Conduit performance helps with that.)

    Test Plan: Ran `differential.revision.search`, saw cost drop from ~195ms to ~170ms locally.

    Reviewers: yelirekim, chad

    Reviewed By: chad

    Maniphest Tasks: T11404

    Differential Revision: https://secure.phabricator.com/D16355