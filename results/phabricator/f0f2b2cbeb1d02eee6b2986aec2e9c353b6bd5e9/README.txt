commit f0f2b2cbeb1d02eee6b2986aec2e9c353b6bd5e9
Author: epriestley <git@epriestley.com>
Date:   Sun Feb 22 13:45:23 2015 -0800

    Start all daemons under a single overseer

    Summary:
    Ref T7352. This moves all the daemons under one overseer. The primary goal is to reduce the minimum footprint of an instance in the Phacility cluster, by reducing the number of processes each instance needs to run on daemon-tier hosts.

    This improves scalability by roughly a factor of 2.

    Test Plan:
      - Ran `phd debug`, `phd launch, `phd start`. Saw normal behavior, with only one total overseer.
      - Fataled dameons and saw the overseer restar them normally.
      - Used `phd status` and `phd stop` and got reasonable results (`phd status` is still a touch off).

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T7352

    Differential Revision: https://secure.phabricator.com/D11857