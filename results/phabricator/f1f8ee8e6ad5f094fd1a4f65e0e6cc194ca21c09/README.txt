commit f1f8ee8e6ad5f094fd1a4f65e0e6cc194ca21c09
Author: epriestley <git@epriestley.com>
Date:   Mon Feb 15 07:20:29 2016 -0800

    Improve subproject/milestone error handling for users who can't create projects

    Summary:
    Fixes T10357.

      - Show a better (more descriptive) error message when a user who can't create projects tries to create a subproject or milestone.
      - Disable the subproject actions if you don't have create permission.

    All this stuff was already enforced properly: this diff doesn't make any actual policy changes, just improves the UI for users who lack permission.

    Test Plan:
      - As an unprivileged user (no "Can Create Projects"), tried to create a subproject or milestone.
      - After patch, got a disabled action, with more specific and helpful error than before.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10357

    Differential Revision: https://secure.phabricator.com/D15274