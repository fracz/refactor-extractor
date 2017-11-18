commit 0e40d1d3e13ba79e61e5313ea4af8241d06cfdaf
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Nov 6 16:30:29 2013 -0800

    Fix issue #11223338: Not retaining service started state while restarting

    When I cleaned up how we maintained the lifecycle of the tracker with a
    service, I broke most tracking of the service restart state.  (Since at
    that point the service is no longer associated with a process, so I
    must clean up the tracker state).  This change introduces a new special
    case for interacting with a service tracker to explicitly tell it when
    a service is being restarted.  It also fixes how we update the process
    state when services are attached to it, so it goes in and out of the
    restarting state correctly.

    In addition:

    - Maybe fix issue #11224000 (APR: Dependent processes not getting added
      to LRU list).  We were not clearing ServiceRecord.app when bringing
      down a service, so if for some reason there were still connections to
      it at that point (which could happen for example for non-create bindings),
      then we would so it when updating the LRU state of that client process.
    - dumpsys procstats's package argument can now be a package or process
      name, and we will dump all relevent information we can find about that
      name.
    - Generally improved the quality of the dumpsys procstats output with its
      various options.
    - Fixed a bug in ActivityManager.dumpPackageState() where it would hang if
      the service was dumping too much, added meminfo to the set of things
      dumped, and tweaked command line options to include more data.
    - Added some more cleaning code to ActiveServices.killServices() to make
      sure we clean out any restarting ServiceRecord entries when a process is
      being force stopped.
    - Re-arranged ActiveServices.killServices() to do the main killing of the
      service first, to avoid some wtf() calls that could happen when removing
      connections.

    Bug: 11223338
    Bug: 11224000

    Change-Id: I5db28561c2c78aa43561e52256ff92c02311c56f