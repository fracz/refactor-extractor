commit 449db8621dd7300993d523faae31e0688fa2f6c9
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Dec 26 21:23:49 2007 +0000

    names refactoring

    --HG--
    rename : src/org/mockito/internal/invocation/InvocationsFinder.java => src/org/mockito/internal/invocation/GlobalInvocationsFinder.java
    rename : src/org/mockito/internal/invocation/ActualInvocationsFinder.java => src/org/mockito/internal/invocation/InvocationsFinder.java
    rename : test/org/mockito/internal/invocation/ActualInvocationsFinderTest.java => test/org/mockito/internal/invocation/InvocationsFinderTest.java
    rename : test/org/mockito/internal/verification/ActualInvocationsFinderStub.java => test/org/mockito/internal/verification/InvocationsFinderStub.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40241