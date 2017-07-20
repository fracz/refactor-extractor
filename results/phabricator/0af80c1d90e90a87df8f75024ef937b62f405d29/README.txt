commit 0af80c1d90e90a87df8f75024ef937b62f405d29
Author: epriestley <git@epriestley.com>
Date:   Fri Mar 6 09:58:26 2015 -0800

    Further improve line grouping in unified views

    Summary:
    Ref T2009. This tweaks things a bit more to improve consecuitive groups of added and removed lines.

    Generally, it gives us "old, old, old, new, new, new" intead of "old, new, old, new, old, new".

    Feelin' real good about having unit tests for this stuff.

    Test Plan: Unit tests, looked at diffs in web UI.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T2009

    Differential Revision: https://secure.phabricator.com/D11994