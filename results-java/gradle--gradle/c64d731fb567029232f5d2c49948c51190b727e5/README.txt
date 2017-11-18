commit c64d731fb567029232f5d2c49948c51190b727e5
Author: Daz DeBoer <daz@gradle.com>
Date:   Tue Aug 2 21:14:19 2016 -0600

    Move some composite-build types back into :core

    These classes effectively form a contract between the
    :launcher and the :toolingApiBuilders subproject. This
    stuff is quite tangled, and should be refactored and
    cleaned up at a later date.

    This change should help fix the build, however.