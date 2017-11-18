commit 8e69257a9c7e9c1781e1f53d8856358ada38921d
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Sep 10 19:06:15 2013 -0700

    Implement #10749688: Improve low memory reporting

    This significantly reworks the logging we do when
    all cached processes are killed:

    - We now collect the list of processes in-place so we
      have a snapshot of exactly when the low memory situation
      happened.
    - In that snapshot we include the key process state: oom
      adj, proc state, adj reasons.
    - The report then asynchronously collects pss information
      for those processes.
    - The ultimate data printed to the log looks like a mix
      between the "dumpsys meminfo" and "dumpsys activity"
      output.  This code no longer uses "dumpsys meminfo"
      itself, so some of that data is no longer included,
      in particular pss organized by allocation type.

    In doing this, I realized that the existing code that is
    supposed to run "procstats" is not currently working.  And
    at that point I realized, really, when we are collecting
    this pss data we'd really like to include all those native
    processes using ghod-only-knows how much RAM.  And guess
    what, we have a list of processes available in
    ProcessCpuTracker.

    So we now also collect and print information for native
    processes, and we also do this for "dumpsys meminfo" which
    really seems like a good thing when we are printing summaries
    of all pss and such.

    I also improved the code for reading /proc/meminfo to be
    able to load all the interesting fields from there, and
    am now printing that as well.

    Change-Id: I9e7d13e9c07a8249c7a7e12e5433973b2c0fdc11