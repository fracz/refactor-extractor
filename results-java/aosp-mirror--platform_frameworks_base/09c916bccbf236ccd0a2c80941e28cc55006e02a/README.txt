commit 09c916bccbf236ccd0a2c80941e28cc55006e02a
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Dec 8 14:50:51 2009 -0800

    Add bindService API to not bring ot foreground.

    Add a new flag for bindService that tells the system to not bring the
    target service's process in to the foreground scheduling class.  This is
    used by the sync system to not cause the current sync adapter to come to
    the foreground as it is running.

    Also some small improvements to the debug output of the process list
    of oom adj and scheduling info.