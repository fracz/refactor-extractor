commit 70c7f650f6d12bb31cd97ed6df5f67474109bc73
Author: iglocska <andras.iklody@gmail.com>
Date:   Wed Jan 29 15:52:09 2014 +0100

    Changes to the logging and scheduling

    - Scheduled tasks for pull / push now working as intended
    - Rescheduling of all tasks fixed
    - protection against the rescheduled task ending up in the past

    - further event history fixes
    - fixed lots of erroneous logging
    - performance improvement with logging (no longer loading controllers for no reason)
    - logging extra actions that weren't logged before (proposal accept / discard, server pull / push)