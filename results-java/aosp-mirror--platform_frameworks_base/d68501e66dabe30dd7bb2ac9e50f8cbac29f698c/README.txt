commit d68501e66dabe30dd7bb2ac9e50f8cbac29f698c
Author: Andrii Kulian <akulian@google.com>
Date:   Tue Jan 10 22:57:27 2017 -0800

    Fix typo that caused excessive display config updates

    Applied incorrect configuration object while doing original
    configuration refactoring.

    Test: bit FrameworksServicesTests:com.android.server.wm.DisplayContentTests
    Test: #testDisplayOverrideConfigUpdate
    Change-Id: Ief5979feebb50f689b6cb532e1dba93e47dcbc40