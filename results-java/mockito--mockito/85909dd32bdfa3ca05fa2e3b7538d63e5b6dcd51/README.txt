commit 85909dd32bdfa3ca05fa2e3b7538d63e5b6dcd51
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Dec 11 21:15:39 2007 +0000

    name refactoring

    --HG--
    rename : src/org/mockito/internal/state/LastArguments.java => src/org/mockito/internal/progress/LastArguments.java
    rename : src/org/mockito/internal/state/MockingProgress.java => src/org/mockito/internal/progress/MockingProgress.java
    rename : src/org/mockito/internal/state/MockingProgressImpl.java => src/org/mockito/internal/progress/MockingProgressImpl.java
    rename : src/org/mockito/internal/state/OngoingStubbing.java => src/org/mockito/internal/progress/OngoingStubbing.java
    rename : src/org/mockito/internal/state/OngoingVerifyingMode.java => src/org/mockito/internal/progress/OngoingVerifyingMode.java
    rename : src/org/mockito/internal/state/ThreadSafeMockingProgress.java => src/org/mockito/internal/progress/ThreadSafeMockingProgress.java
    rename : test/org/mockito/internal/state/MockingProgressImplTest.java => test/org/mockito/internal/progress/MockingProgressImplTest.java
    rename : test/org/mockito/internal/state/OngoingVerifyingModeTest.java => test/org/mockito/internal/progress/OngoingVerifyingModeTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40165