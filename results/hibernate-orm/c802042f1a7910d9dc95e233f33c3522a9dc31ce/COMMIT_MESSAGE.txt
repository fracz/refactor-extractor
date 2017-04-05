commit c802042f1a7910d9dc95e233f33c3522a9dc31ce
Author: Ståle W. Pedersen <stalep@gmail.com>
Date:   Fri Dec 4 03:33:32 2015 +0100

    HHH-10366 Improve performance and reduce CPU load when fetching reference cached objects

    Removed a call to postLoad and afterInitialize for reference cached entities;
    refactor methods, made some private, some final
    try to optimize cached result
    refactor out getPersister so it can be inlined