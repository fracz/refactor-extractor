commit ab4650c4563cd46e65ac37f924fee8febfd759c6
Author: Tyler Gunn <tgunn@google.com>
Date:   Thu Nov 6 20:06:23 2014 -0800

    Conference event package performance improvement.

    - Instead of sending each participant to the telephony conference
    controller, all participants are sent at once.  This way the conference
    only needs to be recalculated once.

    Bug: 18057361
    Change-Id: I86205fc7f1d2648bb180fc7eaf3ad611955952f9