commit bf5437212cc85bf2079c4bcfd796af26a0bba6ae
Author: epriestley <git@epriestley.com>
Date:   Mon May 16 11:53:29 2016 -0700

    When a revision is accepted but has open dependencies, show a note in the list UI

    Summary:
    Ref T10939. I don't think this is hugely important, but it doesn't clutter things up much and it's nice as a hint.

    T4055 was the original request specifically asking for this. It wanted a separate bucket, but I think this use case isn't common/strong enough to justify that.

    I would like to improve Differential's "X depends on Y" feature in the long term. We don't tend to use/need it much, but it could easily do a better and more automatic job of supporting review of a group of revisions.

    Test Plan: {F1426636}

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10939

    Differential Revision: https://secure.phabricator.com/D15930