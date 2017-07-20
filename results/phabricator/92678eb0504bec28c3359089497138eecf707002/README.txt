commit 92678eb0504bec28c3359089497138eecf707002
Author: epriestley <git@epriestley.com>
Date:   Fri Dec 7 16:26:43 2012 -0800

    Improve style of notifications

    Summary:
      - Gets about 25% of the way toward @chad's notification mocks.
        - YES: Hover states, entire notification is a click target, border, header, footer.
        - NO: Profile pictures (lazy), timestamps (want to refactor time code before introducing a new formatting style), app icons (they'd look funny without timestamps I think)
      - Deletes some old files.
      - Mostly trying to get this good enough to turn on by default.

    Test Plan: Looked at notifications. Clicked some notifications.

    Reviewers: chad, btrahan

    Reviewed By: btrahan

    CC: aran

    Differential Revision: https://secure.phabricator.com/D4119