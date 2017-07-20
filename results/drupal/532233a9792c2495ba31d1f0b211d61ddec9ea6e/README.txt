commit 532233a9792c2495ba31d1f0b211d61ddec9ea6e
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sun May 20 19:30:39 2001 +0000

    - Removed includes/timer.inc: it has been integrated in common.inc.

    - Fixed a bug in node.php: UnConeD forgot to update 1 node_get_object().

    - I changed the look of theme_morelink() a bit: it might not look better,
      but at least the output is "correct".

    - Various small improvements.