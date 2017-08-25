commit bb2c22bda5f5c3cb47920e8b55d3c00554138245
Author: moodler <moodler>
Date:   Thu Sep 20 04:52:54 2007 +0000

    I renamed these to help general readability of accesslib:

        $ad -> $accessdata
        has_cap_fad ->  has_capability_in_accessdata()
        aggr_roles_fad  -> aggregate_roles_from_accessdata()

    Resolves MDL-11173