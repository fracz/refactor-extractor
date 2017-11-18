commit a340bfd7a1e639bb50125a62cfb1a14e2565607e
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Tue Aug 2 18:24:49 2016 -0700

    Add historical logging to settings provider

    This change adds historical operations to the dump state
    of the settings provider. The historica operations are
    currently appended only on user-debug and eng builds.

    These change is needed to help diagnose the referred
    bug and improve the settings provider's maintenance.

    bug:30561721

    Change-Id: I58a1ba0d598c4d28adcb5e654ebb78cf947e94db