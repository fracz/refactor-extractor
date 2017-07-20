commit 304d19f92a7bea08573045d6951cefa4b14e7086
Author: epriestley <git@epriestley.com>
Date:   Sun Apr 2 09:53:26 2017 -0700

    After a fulltext write to a particular service fails, keep trying writes to other services

    Summary:
    Ref T12450. Currently, if a write fails, we stop and don't try to write to other index services. There's no technical reason not to keep trying writes, it makes some testing easier, and it would improve behavior in a scenario where engines are configured as "primary" and "backup" and the primary service is having some issues.

    Also, make "no writable services are configured" acceptable, rather than an error. This state is probably goofy but if we want to detect it I think it should probably be a config-validation issue, not a write-time check. I also think it's not totally unreasonable to want to just turn off all writes for a while (maybe to reduce load while you're doing a background update).

    Test Plan:
      - Configured a bad ElasticSearch engine and a good MySQL engine.
      - Ran `bin/search index ... --force`.
      - Saw MySQL get updated even though ElasticSearch failed.

    Reviewers: chad, 20after4

    Reviewed By: 20after4

    Maniphest Tasks: T12450

    Differential Revision: https://secure.phabricator.com/D17599