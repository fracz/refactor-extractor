commit ffdd1411d5deb263f9bea2a8dcadc7881eb2211c
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sun Oct 12 09:54:10 2008 +0000

    massive checkin (worked offline), refactorings around the way verification is done

    --HG--
    rename : src/org/mockito/internal/verification/MissingInvocationVerifier.java => src/org/mockito/internal/verification/MissingInvocationChecker.java
    rename : src/org/mockito/internal/verification/MissingInvocationInOrderVerifier.java => src/org/mockito/internal/verification/MissingInvocationInOrderChecker.java
    rename : src/org/mockito/internal/verification/NumberOfInvocationsVerifier.java => src/org/mockito/internal/verification/NumberOfInvocationsChecker.java
    rename : src/org/mockito/internal/verification/NumberOfInvocationsInOrderVerifier.java => src/org/mockito/internal/verification/NumberOfInvocationsInOrderChecker.java
    rename : test/org/mockito/internal/verification/MissingInvocationVerifierTest.java => test/org/mockito/internal/verification/MissingInvocationCheckerTest.java
    rename : test/org/mockito/internal/verification/MissingInvocationInOrderVerifierTest.java => test/org/mockito/internal/verification/MissingInvocationInOrderCheckerTest.java
    rename : test/org/mockito/internal/verification/NumberOfInvocationsVerifierTest.java => test/org/mockito/internal/verification/NumberOfInvocationsCheckerTest.java
    rename : test/org/mockito/internal/verification/NumberOfInvocationsInOrderVerifierTest.java => test/org/mockito/internal/verification/NumberOfInvocationsInOrderCheckerTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40945