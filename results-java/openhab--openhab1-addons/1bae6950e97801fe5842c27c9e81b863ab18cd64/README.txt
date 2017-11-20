commit 1bae6950e97801fe5842c27c9e81b863ab18cd64
Author: Michael Koch <tensberg@gmx.net>
Date:   Sun Sep 24 18:33:06 2017 +0200

    [fritzboxtr064] add items for dsl/wan statistics (#5221) (#5295)

    * [fritzboxtr064] Refactoring: use map for all services and all item maps for easy lookup (#5221)

    Signed-off-by: Michael Koch <tensberg@gmx.net> (github: tensberg)

    * [fritzboxtr064] Set required execution environment to Java 8 (#5221)

    this is done in preparation for using Java 8 APIs like Optional and Function

    Signed-off-by: Michael Koch <tensberg@gmx.net> (github: tensberg)

    * [fritzboxtr064] Fetch multiple values in one fritzbox request (#5221)

    Rework of the ItemMap concept, which maps a configured item to a call of
    a FritzBox SOAP service. The previous ItemMap only supported a single
    response value per SOAP call. The new MultiItemMap can extract values
    for multiple configured items from a single SOAP service response. This
    avoids multiple calls to the same service if several items are
    configured.

    This change also improves and unifies the  handling of SOAP faults and
    missing values.

    Signed-off-by: Michael Koch <tensberg@gmx.net> (github: tensberg)

    * [fritzboxtr064] Add items for dsl status (#5221)

    Signed-off-by: Michael Koch <tensberg@gmx.net> (github: tensberg)

    * [fritzboxtr064] Add items for WAN status (#5221)

    Signed-off-by: Michael Koch <tensberg@gmx.net> (github: tensberg)

    * [fritzboxtr064] Use long instead of int when parsing numbers (#5221)

    this fixes an integer overflow for some values returned by the FritzBox

    Signed-off-by: Michael Koch <tensberg@gmx.net> (github: tensberg)

    * [fritzboxtr064] Add/update license headers (#5221)

    Signed-off-by: Michael Koch <tensberg@gmx.net> (github: tensberg)

    * [fritzboxtr064] add FritzBox 7330 to the list of tested FritzBoxen

    Signed-off-by: Michael Koch <tensberg@gmx.net> (github: tensberg)

    * [fritzboxtr064] fix log level of of messages which should be warnings

    Signed-off-by: Michael Koch <tensberg@gmx.net> (github: tensberg)