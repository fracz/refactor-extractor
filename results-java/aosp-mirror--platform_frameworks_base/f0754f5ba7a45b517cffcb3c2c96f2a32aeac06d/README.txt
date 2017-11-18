commit f0754f5ba7a45b517cffcb3c2c96f2a32aeac06d
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Jul 21 16:02:07 2011 -0700

    Fix bug where memory trim was not being delivered with correct level.

    Also improve how we handle services, keeping track of whether they showed
    UI and if so putting them immediately on the LRU list.

    Change-Id: I816834668722fc67071863acdb4a7f427a982a08