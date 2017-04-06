commit bc5d6cdff578f3777707854382e5692d03afbdce
Author: Simon Willnauer <simonw@apache.org>
Date:   Tue Jan 19 12:47:34 2016 +0100

    Validate known global settings on startup

    Today we already validate all index level setting on startup. For global
    settings we are not fully there yet since not all settings are registered.
    Yet we can already validate the ones that are know if their values are parseable/correct.
    This is better than nothing and an improvement to what we had before. Over time there will
    be more an dmore setting converted and we can finally flip the switch.