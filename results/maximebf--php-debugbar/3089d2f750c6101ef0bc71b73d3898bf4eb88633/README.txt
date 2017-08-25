commit 3089d2f750c6101ef0bc71b73d3898bf4eb88633
Author: maximebf <maxime.bouroumeau@gmail.com>
Date:   Fri Feb 14 12:40:15 2014 -0300

    refactoring and improvements to resizing and responsivity

    debugbar header is now split in left and right containers for better responsivity
    changed the way the indicators position is handled (not a property of the indicator anymore)
    moved resize-handle out of body
    when closed, the debugbar will always restore to opened state