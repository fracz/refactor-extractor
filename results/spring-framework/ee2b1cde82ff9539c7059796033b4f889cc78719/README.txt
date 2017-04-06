commit ee2b1cde82ff9539c7059796033b4f889cc78719
Author: Costin Leau <cleau@vmware.com>
Date:   Fri Jan 29 14:27:49 2010 +0000

    SPR-6775
    + remove class definitions for sticking around (by forcing eager metadata initialization)
    + improve cache size by eliminating the numbers of method metadata objects created
    + improve lookup access on method metadata