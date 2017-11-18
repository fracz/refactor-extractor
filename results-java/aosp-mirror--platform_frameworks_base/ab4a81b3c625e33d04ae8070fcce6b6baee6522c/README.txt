commit ab4a81b3c625e33d04ae8070fcce6b6baee6522c
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Oct 9 17:59:38 2014 -0700

    Improve some docs, fix some debugging.

    - Add docs to Binder, Messenger, ResultReceier to explain their
      relation (or lack there-of) to process lifecycle.
    - Clarify some aspects of process lifecycle for services.
    - Fix help text of am command.
    - Fix per-package dumping of battery stats to not include history.
    - Fix per-package dumping of proc stats to only include aggregated
      and current stats and fix some formatting.
    - Fix per-process dumping of meminfo to have an option to interpret
      the input as a package, so including all processes that are
      running code of that package.
    - Fix top-level per-package debug output to correctly include all
      of these improvements and give them a little more time (10s) to
      complete for timing out.

    Change-Id: I2a04c0f862bd47b08329443d722345a13ad9b6e2