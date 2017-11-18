commit cb4285537b1bf67b5a248e509d5fe41a6f49282e
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Sep 26 11:07:17 2013 -0700

    Fix issue #10948509: Crash in procstats when there is no data

    Not dealing with the case where there is a null list.

    Also fixed some bugs I found while looking at this:

    - When resetting the stats, we would use a newly computed time stamp
      for the total durations rather than the one we used to reset the
      proc/service entries.  This would result in them being able to be
      slightly > 100%.
    - There was a bug in how we split a single process state into its
      per-package representation, where we would but the cloned process
      state into the new package's entry (instead of properly for its
      own package entry), to be immediately overwritten by the new
      process state we make for that package.  This could result in
      bad data for processes that have multiple packages.
    - There was a bug in resetting service stats, where we wouldn't
      update the overall run timestamp, allowing that time to sometimes
      be > 100%.
    - There was a bug in computing pss data for processes with multiple
      packages, where the pss data was not distributed across all of the
      activity per-package process states.
    - There was a bug in computing the zram information that would cause
      it to compute the wrong value, and then never be displayed.

    Finally a little code refactoring so that ProcessState and ServiceState
    can now share a common implementation for the table of duration values.

    Change-Id: I5e0f4e9107829b81f395dad9419c33257b4f8902