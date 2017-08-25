commit 19608efbb0c14217f12831a1b2d35e68e7a50ff2
Author: Borek Bernard <borekb@gmail.com>
Date:   Wed Mar 2 12:07:11 2016 +0100

    [#719] MergeDriverInstaller: the driver impl can now be forced and global constants were removed (refactored). This also influenced tests. Details:

    - MergeDriverInstaller::installMergeDriver() method now has a $driver parameter through which the code can force the implementation.
    - Tests use this $driver parameter so we could get rid of the brittle `switchDriverToBash()` and `switchDriverToPhp() methods in MergeDriverTestUtils.
    - MergeDriverInstaller no longer uses global constants like VP_PROJECT_ROOT or VERSIONPRESS_MIRRORING_DIR. The code is more explicit and self-contained now.
    - Test were slightly refactored again to remove some unnecessary code.