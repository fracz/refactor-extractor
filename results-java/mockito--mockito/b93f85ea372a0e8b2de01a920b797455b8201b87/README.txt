commit b93f85ea372a0e8b2de01a920b797455b8201b87
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Dec 24 22:23:42 2007 +0000

    another bunch of refactorings, getting rid of TODOs and adding more functional tests

    --HG--
    rename : src/org/mockito/exceptions/verification/VerificationError.java => src/org/mockito/exceptions/verification/InvocationDiffersFromActual.java
    rename : src/org/mockito/exceptions/verification/NoInteractionsWantedError.java => src/org/mockito/exceptions/verification/NoInteractionsWanted.java
    rename : src/org/mockito/exceptions/verification/TooLittleActualInvocationsError.java => src/org/mockito/exceptions/verification/TooLittleActualInvocations.java
    rename : src/org/mockito/exceptions/verification/TooManyActualInvocationsError.java => src/org/mockito/exceptions/verification/TooManyActualInvocations.java
    rename : test/org/mockitousage/PointingStackTraceToActualInvocationTest.java => test/org/mockitousage/PointingStackTraceToActualChunkTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40233