commit 00f4c37ca28e50d5b025d231b8d5d4d0526e781a
Author: mgummelt <mgummelt@fb.com>
Date:   Thu Jul 7 14:51:27 2011 -0700

    Fixed bug resulting in duplicate field names in commit messages

    Summary:
    My earlier diff refactored some code without completely
    respecting the semantics, sometimes resulting in duplicate field names
    returned from differential.getcommitmessage.  This fixes that.

    Test Plan:
    ran "arc diff" with diff causing the bug (commit message
    had an empty Revert Plan: field) and verified no duplicate fields

    Reviewed By: epriestley
    Reviewers: epriestley
    CC: aran, dpepper, epriestley
    Differential Revision: 610