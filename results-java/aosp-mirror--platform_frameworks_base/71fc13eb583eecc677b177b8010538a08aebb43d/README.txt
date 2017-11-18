commit 71fc13eb583eecc677b177b8010538a08aebb43d
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Feb 3 10:50:53 2014 -0800

    More battery history improvements.

    - Better batching of history items.  Fixed problems where empty
      entries would be created because state toggles got lost.
    - The string pool is now a HistoryTag pool, containing both a string
      and uid; now an entry only requires 16 bits in the history data.
    - Acquiring the first wake lock also now includes a HistoryTag
      identifying who did the aquisition.
    - Cleaned up printing of signal strengths and cell radio types.
    - There was code that tried to allow you to add new history entries
      while iterating the history...  but these should never happen
      together, so turned that into a failure...  and fixed an issue
      where we could leave the battery stats in a state where it
      thinks it is continually iterating.

    Change-Id: I1afa57ee2d66b186932c502dbdd633cdd4aed353