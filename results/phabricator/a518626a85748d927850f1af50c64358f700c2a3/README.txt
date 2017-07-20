commit a518626a85748d927850f1af50c64358f700c2a3
Author: epriestley <git@epriestley.com>
Date:   Thu Nov 21 12:58:58 2013 -0800

    Slightly improve behavior for unverified + unapproved users

    Summary: Ref T4140. Allow unapproved users to verify their email addresses. Currently, unapproved blocks email verification, but should not.

    Test Plan: Clicked email verification link as an unapproved user, got email verified.

    Reviewers: btrahan, chad

    Reviewed By: chad

    CC: aran

    Maniphest Tasks: T4140

    Differential Revision: https://secure.phabricator.com/D7618