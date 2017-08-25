commit 4f3e38e745c389af48525f6355a19359a374e987
Author: Martin Langhoff <martin.langhoff@remote-learner.net>
Date:   Tue Jun 11 20:56:39 2013 -0400

    MDL-40266 improve emulate_bound_params() for mysqli

    Looping over large numbers of items with array_shift() is expensive.
    Reverse the array and fetch items from the top of the pile.