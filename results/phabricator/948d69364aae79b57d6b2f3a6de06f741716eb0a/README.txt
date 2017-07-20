commit 948d69364aae79b57d6b2f3a6de06f741716eb0a
Author: epriestley <git@epriestley.com>
Date:   Mon May 4 10:08:49 2015 -0700

    Manage date control enabled state as part of DateControlValue

    Summary: Ref T8024. Allow `DateControlValue` to manage enabled/disabled state, so we can eventually delete the copy of this logic in `DateControl`.

    Test Plan:
      - Used Calendar ApplicationSearch queries to observe improved behaviors:
        - Error for invalid start date, if enabled.
        - Error for invalid end date, if enabled.
        - Error for invalid date range, if both enabled.
        - When submitting an invalid date (for example, with the time "Tea Time"), form retains invalid date verbatim instead of discarding information.
      - Created an event, using existing date controls to check that I didn't break anything.

    Reviewers: chad, lpriestley, btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T8024

    Differential Revision: https://secure.phabricator.com/D12673