commit 102b334c97d8dc924883aa97ed80b0dab67d9487
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sun Dec 9 18:28:43 2007 +0000

    package structure refactoring

    --HG--
    rename : src/org/mockito/exceptions/Strings.java => src/org/mockito/exceptions/StringJoiner.java
    rename : src/org/mockito/exceptions/InvalidUseOfMatchersException.java => src/org/mockito/exceptions/misusing/InvalidUseOfMatchersException.java
    rename : src/org/mockito/exceptions/MissingMethodInvocationException.java => src/org/mockito/exceptions/misusing/MissingMethodInvocationException.java
    rename : src/org/mockito/exceptions/NotAMockException.java => src/org/mockito/exceptions/misusing/NotAMockException.java
    rename : src/org/mockito/exceptions/UnfinishedStubbingException.java => src/org/mockito/exceptions/misusing/UnfinishedStubbingException.java
    rename : src/org/mockito/exceptions/UnfinishedVerificationException.java => src/org/mockito/exceptions/misusing/UnfinishedVerificationException.java
    rename : src/org/mockito/exceptions/NumberOfInvocationsError.java => src/org/mockito/exceptions/verification/NumberOfInvocationsError.java
    rename : src/org/mockito/exceptions/StrictVerificationError.java => src/org/mockito/exceptions/verification/StrictVerificationError.java
    rename : src/org/mockito/exceptions/TooLittleActualInvocationsError.java => src/org/mockito/exceptions/verification/TooLittleActualInvocationsError.java
    rename : src/org/mockito/exceptions/TooManyActualInvocationsError.java => src/org/mockito/exceptions/verification/TooManyActualInvocationsError.java
    rename : src/org/mockito/exceptions/VerificationError.java => src/org/mockito/exceptions/verification/VerificationError.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40151