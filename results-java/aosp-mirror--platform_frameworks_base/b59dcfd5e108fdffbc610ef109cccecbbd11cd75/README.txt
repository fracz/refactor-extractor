commit b59dcfd5e108fdffbc610ef109cccecbbd11cd75
Author: Craig Mautner <cmautner@google.com>
Date:   Mon May 6 13:12:58 2013 -0700

    Call ensureActivitiesVisibleLocked from supervisor.

    - Don't call ActivityStack.ensureActivitiesVisibleLocked directly.
    Instead call ActivityStackSupervisor.ensureActivitiesVisibleLocked.

    - Add detecting monochrome screenshots to black screenshots.

    - minor refactors.

    Change-Id: I050b1cd40cacaab451f1460a77a82125a8077ff2