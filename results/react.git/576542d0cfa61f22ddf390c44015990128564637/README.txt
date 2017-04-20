commit 576542d0cfa61f22ddf390c44015990128564637
Author: Sebastian Markbåge <sebastian@calyptus.eu>
Date:   Tue Nov 1 18:54:11 2016 -0700

    Handle the radio button case completely in the asap case (#8170)

    Instead of scheduling individual callbacks to asap, we schedule one and
    then do all the work in that one.

    I'm doing this for architectural refactoring reasons.

    Nevertheless, I'm adding a contrived unit test that this fixes. :)