commit 59069349f899e36c05149beaebd49967b48be45e
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Dec 11 21:09:55 2007 +0000

    name refactoring

    --HG--
    rename : src/org/mockito/internal/state/MockitoState.java => src/org/mockito/internal/state/MockingProgress.java
    rename : src/org/mockito/internal/state/MockitoStateImpl.java => src/org/mockito/internal/state/MockingProgressImpl.java
    rename : src/org/mockito/internal/state/ThreadSafeMockitoState.java => src/org/mockito/internal/state/ThreadSafeMockingProgress.java
    rename : test/org/mockito/internal/state/MockitoStateImplTest.java => test/org/mockito/internal/state/MockingProgressImplTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40164