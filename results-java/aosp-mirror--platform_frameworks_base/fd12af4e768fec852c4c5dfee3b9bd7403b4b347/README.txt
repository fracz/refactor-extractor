commit fd12af4e768fec852c4c5dfee3b9bd7403b4b347
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Aug 27 00:44:33 2009 -0700

    Various tweaks to try to improve low memory behavior.

    - Reduce the amount that we ask processes to GC after a significant
      operation occurs, but introducing a minimum time between GCs and
      using this in various ways to schedule them.

    - Don't spam all of the processes with onLowMemory().  Now deliver
      these using the same gc facility, so we do the processes one at a
      time, and don't allow the same process to get this call more than
      once a minute.

    - Increase the time a service must run before we will reset its
      restart delay to 30 minutes (from 10).

    - Increase the restart delay multiplication factor from 2 to 4.

    - Ensure that we don't restart more than one service every 10 seconds
      (unless some external event causes a service's process to be started
      for some other reason of course).

    - Increase the amount of time that a service must run before we
      decide to lower it to a background process.

    And some other things:

    - Catch IllegalArgumentException in ViewRoot like we do for no
      resources to avoid the system process crashing.

    - Fix a number of places where we were missing breaks between the
      activity manager's message dispatch func(!!).

    - Fix reason printed for processes in the background.

    - Print the list of processing waiting to GC.