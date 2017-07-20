commit b3ae48d8ca2dfe5d2fa28bdad9f86af0251a7cd9
Author: epriestley <git@epriestley.com>
Date:   Wed Jun 17 11:25:01 2015 -0700

    Improve Differential query plans

    Summary:
    Ref T8575. We run a big "(A) UNION (B)" query on the home page and on the main Differential page.

    "A" can always be improved by using `%Ls`, so it can use the second half of the `(authorPHID, status)` key.

    "B" can sometimes be improved if the fraction of open revisions is smaller than the fraction of revisions you are reviewing. This is true for me on secure.phabricator.com (I'm a reviewer, either directly or via 'Blessed Reviewers', on about 80% of revisions, but <5% are open). In these cases, a `(status, phid)` key is more efficient.

    Test Plan: Tweaked queries and added keys on this server, saw less derpy query plans and performance.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T8575

    Differential Revision: https://secure.phabricator.com/D13325