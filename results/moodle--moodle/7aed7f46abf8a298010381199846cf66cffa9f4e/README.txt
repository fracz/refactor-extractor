commit 7aed7f46abf8a298010381199846cf66cffa9f4e
Author: Petr Škoda <commits@skodak.org>
Date:   Mon Jul 1 08:52:03 2013 +0200

    MDL-40266 improve emulate_bound_params() for mssql driver

    Looping over large numbers of items with array_shift() is expensive.
    Reverse the array and fetch items from the top of the pile.

    Credit goes to Martin Langhoff for original mysqli fix.