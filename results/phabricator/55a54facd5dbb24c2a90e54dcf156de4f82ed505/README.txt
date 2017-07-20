commit 55a54facd5dbb24c2a90e54dcf156de4f82ed505
Author: epriestley <git@epriestley.com>
Date:   Tue Dec 6 03:53:15 2016 -0800

    Use PhabricatorCachedClassMapQuery in Conduit method lookups

    Summary: Ref T11954. Depends on D16994. This implements the Conduit method cache described in that revision for a small global Conduit performance improvement.

    Test Plan: Verified Conduit has the same behavior at lower cost. See D16994 for details.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11954

    Differential Revision: https://secure.phabricator.com/D16995