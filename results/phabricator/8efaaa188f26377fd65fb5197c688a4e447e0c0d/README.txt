commit 8efaaa188f26377fd65fb5197c688a4e447e0c0d
Author: epriestley <git@epriestley.com>
Date:   Sun Jan 24 07:07:17 2016 -0800

    Move user editing/management actions to a separate "Manage" item, like projects

    Summary: This improves consistency (by making this UI more similar to the projects UI) and gives us more flexibility the next time we update user profiles.

    Test Plan:
    {F1068889}

    Took all the actions (probably?) to check that all the redirects were updated.

    Reviewers: chad

    Reviewed By: chad

    Differential Revision: https://secure.phabricator.com/D15104