commit b983901d48ffd4adb8afffa54d6a803de79efd5a
Author: Petr Škoda <commits@skodak.org>
Date:   Mon Jul 1 08:50:51 2013 +0200

    MDL-40266 improve emulate_bound_params() for sqlsrv driver

    Looping over large numbers of items with array_shift() is expensive.
    Reverse the array and fetch items from the top of the pile.

    Credit goes to Martin Langhoff for original mysqli fix.