commit 2a671ac905e97d108e53e11856b01356f9248cc8
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Thu Sep 15 18:21:32 2011 -0700

    MiltiWaveView accessibility improvement.

    Now hover events behave s motion events in touch exploration mode.
    The use no longer needs to find the handle and tap + hold to engage.
    It is enough to move on top of it.

    Now the handle is engaged, i.e. can be dragged, when the user passes
    on top of it for both accessibility enabled and disabled.

    bug:5253968

    Change-Id: If70b44cb6db8a38432216b46ecddefb3e2bc3825