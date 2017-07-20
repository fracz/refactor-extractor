commit ba9808942611098a6d48412aa71099399aa7eab9
Author: Aurelijus <aurelijus@astdev.lt>
Date:   Thu May 10 09:28:19 2012 +0200

    Branch view improvements.

    Summary:
    Sorting by last commit date
    Branch view limit to 25 branches
    All branches table page with pagination on Git

    Test Plan:
    * Check repository view for expected behavior on branch table view
    * Check all branches page & test pagination

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: aran, Koolvin

    Maniphest Tasks: T1200

    Differential Revision: https://secure.phabricator.com/D2442