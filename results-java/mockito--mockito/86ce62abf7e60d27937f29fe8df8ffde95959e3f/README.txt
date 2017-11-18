commit 86ce62abf7e60d27937f29fe8df8ffde95959e3f
Author: Brice Dutheil <brice.dutheil@gmail.com>
Date:   Sun Jan 12 02:21:05 2014 +0100

    issue 399 fixes serialization for deep stub answer

     - disables generics deep stub when serialization occurs (not supported yet)
     - also refactored a bit the initialization of this answer