commit 7b3bf370248e480c037bb392049b6ea0162fddfa
Author: Daz DeBoer <darrell.deboer@gradleware.com>
Date:   Sat Aug 23 17:54:32 2014 -0600

    Further improvements to component selection rules infrastructure

    - Moved rule processing into a separate class
    - Version matching is now a built-in selection rule
    - Custom rules that don’t require metadata are applied before version matching that requires metadata