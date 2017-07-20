commit 28b606394be427ae27e0467c4c10f91fff2d6348
Author: epriestley <git@epriestley.com>
Date:   Wed Jan 4 07:18:16 2012 -0800

    Fix undefined variable warning in unit test field

    Summary:
    See D1295. $unit_messages may be undefined.

    I'll see if I can improve the visibility of warnings, the red dot in DarkConsole
    is easy to miss right now. See T734.

    Test Plan: Loaded a revision with no unit failures, didn't receive a warning.

    Reviewers: nh, btrahan, jungejason

    Reviewed By: btrahan

    CC: aran, btrahan

    Differential Revision: https://secure.phabricator.com/D1306