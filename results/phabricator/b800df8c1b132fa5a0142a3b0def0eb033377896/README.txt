commit b800df8c1b132fa5a0142a3b0def0eb033377896
Author: epriestley <git@epriestley.com>
Date:   Wed May 9 10:29:37 2012 -0700

    Simplify daemon management: "phd start"

    Summary:
      - Merge CommitTask daemon into PullLocal daemon. This is another artifact of past instability (and order-dependent parsers). We still publish to the timeline, although this was the last consumer. Long term we'll probably delete timeline and move to webhooks, since everyone who has asked about this stuff has been eager to trade away the durability and ordering of the timeline for the ease of use of webhooks. There's also no reason to timeline this anymore since parsing is no longer order-dependent.
      - Add `phd start` to start all the daemons you need. Add `phd restart` to restart all the daemons you need. So cool~
      - Simplify and improve phd and Diffusion daemon documentation.

    Test Plan:
      - Ran `phd start`.
      - Ran `phd restart`.
      - Generated/read documentation.
      - Imported some stuff, got clean parses.

    Reviewers: btrahan, csilvers

    Reviewed By: csilvers

    CC: aran, jungejason, nh

    Differential Revision: https://secure.phabricator.com/D2433