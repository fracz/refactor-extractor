commit 9248e95a2f03afe4cded90f0209ab825f253dd37
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Jan 5 18:27:40 2011 -0800

    DO NOT MERGE Cherry-pick of Ie4026a7c back to GB

    Original change description follows:
    -----------------------------------

    Implement issue #3326435: Battery stats improvements

    Keep track of discharge while screen is on vs. off.

    Checkin looks like:

    5,0,u,dc,1,1,1,0

    The last four numbers are, from left:

    - Maximum battery drain over time period.
    - Minimum battery drain over time period.
    - Battery drain while screen was on.
    - Battery drain while screen was off.

    Change-Id: Ie3cfe52df29b3f28ba8dc3350abe6cc967c76324