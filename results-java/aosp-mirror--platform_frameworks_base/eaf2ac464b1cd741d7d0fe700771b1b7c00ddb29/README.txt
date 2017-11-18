commit eaf2ac464b1cd741d7d0fe700771b1b7c00ddb29
Author: Dianne Hackborn <hackbod@google.com>
Date:   Fri Feb 7 13:01:07 2014 -0800

    Battery stats: more events, fixes.

    Add new history events for top application package and
    foreground application packages.

    Doing this involved a fair amount of improvement to history
    events.  The event code is now separated out to have "start"
    and "finish" identifies, and we use that to now keep track
    of which events are active.  With that, when resetting the
    stats, we can spit out all of the currently active events at
    the front of the new history.

    Also fixed some problems when I re-arranged the history delta
    int bits that were conflicting with the packing of the battery
    status bits.  These packing structures are changed to work
    together correctly.

    Change-Id: Ic8b815060dd8a50ff4a0a209efc2e1044215cd88