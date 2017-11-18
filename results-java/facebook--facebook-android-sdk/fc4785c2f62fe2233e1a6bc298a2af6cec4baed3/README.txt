commit fc4785c2f62fe2233e1a6bc298a2af6cec4baed3
Author: Vijaye Raji <vijaye@fb.com>
Date:   Thu Feb 16 16:41:49 2012 -0800

    Fixing Token refresh issue in SDK

    Summary:
    The TokenRefresh intent is exposed as a service, but we were validating it as
    an activity.  Fixign that and refactoring code. This code should have never
    worked.

    Note that without the FB app the refresh token feature will not work.  If that
    is necessary, it's not part of this diff.

    Test Plan: Verify that hackbook can login & refresh token on the emulator.

    Reviewers: mmarucheck, yariv, ttung, raghuc1, trvish, pfung

    Reviewed By: mmarucheck

    CC: gregschechte, jacl, lshepard

    Differential Revision: https://phabricator.fb.com/D410960

    Task ID: 926377