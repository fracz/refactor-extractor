commit 5608ca4006860f9d88b0d67f25e7e19b6deef221
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sun Dec 9 18:06:54 2007 +0000

    package structure refactoring

    --HG--
    rename : src/org/mockito/exceptions/CommonStackTraceRemover.java => src/org/mockito/exceptions/parents/CommonStackTraceRemover.java
    rename : src/org/mockito/exceptions/HasStackTrace.java => src/org/mockito/exceptions/parents/HasStackTrace.java
    rename : src/org/mockito/exceptions/MockitoAssertionError.java => src/org/mockito/exceptions/parents/MockitoAssertionError.java
    rename : src/org/mockito/exceptions/MockitoException.java => src/org/mockito/exceptions/parents/MockitoException.java
    rename : src/org/mockito/exceptions/StackTraceFilter.java => src/org/mockito/exceptions/parents/StackTraceFilter.java
    rename : test/org/mockito/exceptions/MockitoErrorTest.java => test/org/mockito/exceptions/MockitoExceptionTest.java
    rename : test/org/mockito/exceptions/MockitoAssertionErrorTest.java => test/org/mockito/exceptions/parents/MockitoAssertionErrorTest.java
    rename : test/org/mockito/exceptions/StackTraceBuilder.java => test/org/mockito/exceptions/parents/StackTraceBuilder.java
    rename : test/org/mockito/exceptions/StackTraceRemoverTest.java => test/org/mockito/exceptions/parents/StackTraceRemoverTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40149