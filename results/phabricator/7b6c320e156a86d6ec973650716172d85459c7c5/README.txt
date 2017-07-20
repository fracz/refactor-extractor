commit 7b6c320e156a86d6ec973650716172d85459c7c5
Author: epriestley <git@epriestley.com>
Date:   Fri May 1 13:19:43 2015 -0700

    Skeleton for "Multimeter", a performance sampling application

    Summary:
    Ref T6930. This application collects and displays performance samples -- roughly, things Phabricator spent some kind of resource on. It will collect samples on different types of resources and events:

      - Wall time (queries, service calls, pages)
      - Bytes In / Bytes Out (requests)
      - Implicit requests to CSS/JS (static resources)

    I've started with the simplest case (static resources), since this can be used in an immediate, straghtforward way to improve packaging (look at which individual files have the most requests recently).

    There's no aggregation yet and a lot of the data isn't collected properly. Future diffs will add more dimension data (controllers, users), more event and resource types (queries, service calls, wall time), and more display options (aggregation, sorting).

    Test Plan: {F389344}

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T6930

    Differential Revision: https://secure.phabricator.com/D12623