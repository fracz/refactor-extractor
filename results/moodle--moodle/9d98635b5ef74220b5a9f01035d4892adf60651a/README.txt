commit 9d98635b5ef74220b5a9f01035d4892adf60651a
Author: stronk7 <stronk7>
Date:   Fri Oct 27 16:56:34 2006 +0000

    Minor improvements to the get_record() cache. Part of MDL-7196

    1) Check the cache issset() before unset() it.
    2) In the set_field() function, if fieldX = 'id',
       just delete such element from the cache,
       else the whole table
    3) Add some more unset() operations against the cache in
       the delete_xxxx() dmllib functions.

    Merged from MOODLE_17_STABLE