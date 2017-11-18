commit d635a4ae20014b6ff52d8b05e7f4f622808d4efd
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Wed May 3 15:21:26 2017 +0200

    Fix snapshots for secure windows

    First, also draw system bar backgrounds when drawing a fake
    snapshot. For that, refactor the drawing into a separate class so
    it can be reused. Also enable fake snapshots for secure windows.

    Test: com.android.server.wm.TaskSnapshotControllerTest
    Test: com.android.server.wm.TaskSnapshotSurfaceTest
    Test: Secure activity with resuming delay, make sure system bars
    are covered when reopening app.

    Bug: 35710126
    Change-Id: I2f0ebc7e7acb80015780a4e882f0a472599efa30