commit ab9c1b73b532b761dc3df9fa52c179ce2fade4bf
Author: epriestley <git@epriestley.com>
Date:   Mon Feb 20 12:48:37 2017 -0800

    Fix bad JS rendering in "Allow Desktop Notifications" workflow

    Summary:
    See downstream <https://phabricator.kde.org/T5404>. This code was doing some `.firstChild` shenanigans which didn't survive some UI refactoring.

    This whole UI is a little iffy but just unbreak it for now.

    Test Plan: Allowed and rejected desktop notifications, got largely reasonable UI rendering.

    Reviewers: chad

    Reviewed By: chad

    Differential Revision: https://secure.phabricator.com/D17388