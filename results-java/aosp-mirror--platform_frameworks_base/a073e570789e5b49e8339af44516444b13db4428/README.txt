commit a073e570789e5b49e8339af44516444b13db4428
Author: Jim Miller <jaggies@google.com>
Date:   Wed May 23 17:03:27 2012 -0700

    Fix 6398209: General animation improvements for swipe to search

    This cleans up the animation for swipe to search from the navbar.  In particular:
    1. Wait for initial animation to finish if gesture was too quick.
    2. Better fade animation
    3. Hide background and fade in when ring is selected
    4. Smoother target and outer ring animation when switching between states.

    Change-Id: I401197760cf9f06b6ff3e1cdb80bee86a03ef276