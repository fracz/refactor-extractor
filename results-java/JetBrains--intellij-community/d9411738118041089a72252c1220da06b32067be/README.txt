commit d9411738118041089a72252c1220da06b32067be
Author: Konstantin Aleev <konstantin.aleev@jetbrains.com>
Date:   Tue Sep 26 15:46:21 2017 +0300

    IDEA-179331 RunDashboard: improve Run/Debug actions enablement

    - Disable Run/Debug actions when run configuration is starting
    - Check that active execution target can run configuration