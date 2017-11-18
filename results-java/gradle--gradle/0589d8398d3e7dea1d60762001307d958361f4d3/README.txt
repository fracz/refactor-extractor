commit 0589d8398d3e7dea1d60762001307d958361f4d3
Author: Daz DeBoer <darrell.deboer@gradleware.com>
Date:   Tue Sep 16 13:38:55 2014 -0600

    Revert breaking changes to InvalidActionClosureException

    - Use InvalidUserCodeException for invalid component selection rule closure
       - This is temporary pending improvements to the component selection rule Java API

    +review REVIEW-5137