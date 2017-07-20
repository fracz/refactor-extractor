commit 1bdf9885562e85982e8ad78612b82139db58934d
Author: epriestley <git@epriestley.com>
Date:   Thu Mar 3 04:34:47 2016 -0800

    Convert DrydockBlueprints to EditEngine

    Summary:
    Ref T10457. Fixes T10024. This primarily just modernizes blueprints to use EditEngine.

    This also fixes T10024, which was an issue with stored properties not being flagged correctly.

    Also slightly improves typeaheads for blueprints (more information, disabled state).

    Test Plan:
      - Created and edited various types of blueprints.
      - Set and removed limits.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10024, T10457

    Differential Revision: https://secure.phabricator.com/D15390