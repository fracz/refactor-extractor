commit 0944984c36934c6b5431383d1b6a719b64338074
Author: Casey Burkhardt <caseyburkhardt@google.com>
Date:   Wed Feb 18 12:18:15 2015 -0800

    Refinements to magnification for improved wearable support.

    This change refactors ScreenMagnifier to use resources for its triple-tap
    adjustment and scale threshold values.  New values more appropriate for
    wearable form factors are supplied.  This also fixes a bug in the triple-
    tap detection logic where the incorrect ViewConfiguration value for the
    tap threshold was used, prematurely disqualifying some touch events as
    potential taps.

    Change-Id: If47e556aadb5beb1bad24644122560c6fbe33bad