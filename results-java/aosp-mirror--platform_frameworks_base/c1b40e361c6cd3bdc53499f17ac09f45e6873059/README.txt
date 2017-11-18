commit c1b40e361c6cd3bdc53499f17ac09f45e6873059
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Jan 5 18:27:40 2011 -0800

    Implement issue #3326435: Battery stats improvements

    Keep track of discharge while screen is on vs. off.

    Checkin looks like:

    5,0,u,dc,1,1,1,0

    The last four numbers are, from left:

    - Maximum battery drain over time period.
    - Minimum battery drain over time period.
    - Battery drain while screen was on.
    - Battery drain while screen was off.

    Change-Id: Ie4026a7cc8aabb2a6d47d03d2e278aa51c2d5ddf