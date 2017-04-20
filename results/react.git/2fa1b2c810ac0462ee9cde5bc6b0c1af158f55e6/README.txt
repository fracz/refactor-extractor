commit 2fa1b2c810ac0462ee9cde5bc6b0c1af158f55e6
Author: Benjamin Woodruff <bgw@fb.com>
Date:   Thu Jun 4 13:42:30 2015 -0700

    Attempt to simplify/fix warnAndMonitorForKeyUse

    > The two callers of this function have different warning configs
    > internally (static_upstream/core/createWarning.js) so we can't sync it
    > like this without changing behavior. We should just split this out
    > into two separate warning calls probably – this code is a little
    > overabstracted.

    https://github.com/facebook/react/pull/4021#discussion_r31690020
    @spicyj

    I think completely removing warnAndMonitorForKeyUse is a bit difficult, without
    duplicating a ton of code. This at least ensures that the format string passed
    to `warning` is unique. Plus, because the FB internal code in question only
    matches the beginning of the format string, I think there should be zero
    internal changes that need to be made to support this refactor.