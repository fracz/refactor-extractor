commit b3d1ecebc7f8a6d26405d9d94c9a9b62b707c049
Author: epriestley <git@epriestley.com>
Date:   Thu Mar 5 16:24:04 2015 -0800

    Fix another issue with line order on unified diffs

    Summary: This improves some cases with interleaved added and removed lines, and adds test coverage.

    Test Plan:
      - Added and executed unit tests.
      - Viewed raw diff and saw sensible/expected output.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Differential Revision: https://secure.phabricator.com/D11992