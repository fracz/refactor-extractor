commit 6aca73fa1ba25a9edd13234b4ee008cd3d5b819e
Author: Christopher Manning <manning@cs.stanford.edu>
Date:   Sun Jun 25 06:54:42 2017 -0700

    Hopefully improve Spanish sentence breaking without harming anything else. As a combination of the weird KBP setup (where newlines are not registered as sentenceBoundaries but are discarded anyway) and the use of straight quotes in Spanish, the placement of quotes into sentences wasn't as good as you might hope. This fixes this for the Spanish case and hopefully doesn't break other cases.