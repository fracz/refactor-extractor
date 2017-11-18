commit c8a8c2a3be4aa473affd565311916953db27ec9b
Author: Ulf Adams <ulfjack@google.com>
Date:   Fri Aug 21 11:03:37 2015 +0000

    Prefer RuleContext.getFragment over BuildConfiguration.getFragment.

    This improves the coverage of the legality check in RuleContext.getFragment.

    --
    MOS_MIGRATED_REVID=101208822