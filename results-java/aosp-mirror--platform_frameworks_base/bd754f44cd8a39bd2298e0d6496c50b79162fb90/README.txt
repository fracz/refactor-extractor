commit bd754f44cd8a39bd2298e0d6496c50b79162fb90
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Jul 23 15:52:36 2013 -0700

    Fix bug that was causing us to lose total memory buckets.

    When safely resetting stats after committing them, we were
    mistakenly clearing the current memory state so we would lose
    that total memory time until the memory state changes again.

    Also improve the summary output to print percentages, which
    make more sense for that display.

    Change-Id: I0fe45fd78e97ec8b94976170dd42f4ed345a5899