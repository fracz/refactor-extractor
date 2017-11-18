commit 099bc627c463d9941e23e480f25a78a154429c55
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Jan 22 13:39:16 2014 -0800

    Battery stats improvements.

    - Adjust total power use when there is unaccounted power so that our
      percentages don't end up > 100%.
    - Fix accounting of isolated uids to be against the owning real app
      uids.
    - Rework how we put cpu use into the battery stats to no longer need
      this uid name cache that can confuse the uid it is associated with.
    - Finish implementing events in the history, adding a string pool and
      reading/writing/dumping them.
    - Add first two events: processes starting and finishing.
    - Fix alarm manager reporting of wakeup alarms to be adjusted by the
      WorkSource associated with the alarm, so they are blamed on the
      correct app.
    - New "--history" dump option allows you to perform a checkin of
      only the history data.
    - Fixed BitDescription bug that would cause incorrect printing of
      changes in some states.

    Change-Id: Ifbdd0740132ed178033851c58f165adc0d50f716