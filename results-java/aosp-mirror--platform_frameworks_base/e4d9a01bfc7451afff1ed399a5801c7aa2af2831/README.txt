commit e4d9a01bfc7451afff1ed399a5801c7aa2af2831
Author: Dan Morrill <morrildl@google.com>
Date:   Thu Mar 28 18:10:43 2013 -0700

    Phase 1 of refactoring SystemServer.

    SystemServer is currently a monolithic class that brings up key system
    services. This change is the first phase of refactoring it to be more
    configurable. Specifically, it adds a set of on/off switches used to control
    startup of individual services. Future plans include finer grained controls
    and a more explicit and consistent startup sequence for these services.

    Change-Id: I7299f5ce7d7b74a34eb56dffb788366fbc058532