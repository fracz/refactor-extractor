commit ea481beda699165a7050ddcc37b10841d33ed9e6
Author: Prashant Malani <pmalani@google.com>
Date:   Wed Jan 7 18:30:27 2015 -0800

    Improve the circularMask for round displays

    The mask was earlier drawn as a thin ring at the display periphery.
    This had undesirable effects when screenshots were taken, as the ring
    was seen, as well as display content beyond it.

    This patch modifies the mask to be a black canvas with portions erased
    to improve the screenshot image.

    Bug: 18772987
    Change-Id: I25ef6387879613354308e015446fe325ed8c4515