commit b8b7e583adcea63557847c505aeafa2f81f4b7b4
Author: epriestley <git@epriestley.com>
Date:   Mon Dec 9 13:22:22 2013 -0800

    Show a queue utilization statistic in the Daemon console

    Summary:
    This came up recently in a discussion with @lifeihuang, and then tangentally with @hach-que. Make it easier for users to get a sense of whether they might need to add more daemons. Although we've improved the transparency of daemons, it's not easy for non-experts to determine at a glance how close to overflowing the queue is.

    This number is approximate, but should be good enough for determining if your queue is more like 25% or 95% full.

    If this goes over, say, 80%, it's probably a good idea to think about adding a couple of daemons. If it's under that, you should generally be fine.

    Test Plan: {F88331}

    Reviewers: btrahan, hach-que, lifeihuang

    Reviewed By: btrahan

    CC: hach-que, lifeihuang, aran, chad

    Differential Revision: https://secure.phabricator.com/D7747