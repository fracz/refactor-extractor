commit 133933f6ea0c1979e56e71e47a2fa7526fbe2f75
Author: Mark Clark <mr.guessed@gmail.com>
Date:   Mon Jun 6 00:19:22 2016 -0700

    [mios] openHAB2 delayed startup/reload improvements under compat layer. (#4446)

    Under the compat1x layer of OH 2, the MiOS binding was seeing a
    different startup sequence than when run natively under OH 1.x.

    This change adds a delay to the startup processing to allow Item loading
    to stabilize before attempting to load data from the MiOS Unit, to avoid
    the sequencing issues.

    Community discussion:
    * https://community.openhab.org/t/openhab2-new-issue-with-the-startup-process-mios-binding/8320

    Signed-off-by: Mark Clark <mr.guessed@gmail.com> (github: mrguessed)