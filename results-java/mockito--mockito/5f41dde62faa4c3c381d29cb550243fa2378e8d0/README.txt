commit 5f41dde62faa4c3c381d29cb550243fa2378e8d0
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Nov 17 23:28:25 2007 +0000

    some refactorings, enabled some ingnored tests

    --HG--
    rename : src/org/mockito/exceptions/NotAMockMethodException.java => src/org/mockito/exceptions/MissingMethodInvocationException.java
    rename : src/org/mockito/internal/ExpectedInvocation.java => src/org/mockito/internal/InvocationWithMatchers.java
    rename : src/org/mockito/internal/MockitoOperations.java => src/org/mockito/internal/MockitoState.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%4030