commit cc3e8fd01ffccf1be7a011724b4dbd7a8deea929
Author: rMalie <raphael.malie@prestashop.com>
Date:   Fri Dec 30 10:58:25 2011 +0000

    // Remove cleanPositions() in modules when a hook module is added (since the hook module go in last position, we don't need to recalculate all positions) -> greatly improve module installation phase in new installer