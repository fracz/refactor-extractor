commit 2c02cdaa7fa549c31dfa22827ac9fc3ff9dcf798
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Nov 16 23:20:01 2007 +0000

    -killed/refactored some tests

    --HG--
    rename : test/org/easymock/InvocationTest.java => test/org/mockito/InvocationTest.java
    rename : test/org/mockito/vs/easymock/ArticleCalculator.java => test/org/mockito/sample/ArticleCalculator.java
    rename : test/org/mockito/vs/easymock/ArticleDatabase.java => test/org/mockito/sample/ArticleDatabase.java
    rename : test/org/mockito/vs/easymock/ArticleManager.java => test/org/mockito/sample/ArticleManager.java
    rename : test/org/mockito/vs/easymock/MockitoVsEasyMockTest.java => test/org/mockito/sample/MockitoSampleTest.java
    rename : test/org/easymock/IMethods.java => test/org/mockito/usage/IMethods.java
    rename : test/org/mockito/ReplacingObjectMethodsTest.java => test/org/mockito/usage/ReplacingObjectMethodsTest.java
    rename : test/org/easymock/UsingVarargsTest.java => test/org/mockito/usage/UsingVarargsTest.java
    rename : test/org/easymock/ComparableMatchersTest.java => test/org/mockito/usage/matchers/ComparableMatchersTest.java
    rename : test/org/mockito/usage/InvalidUseOfMatchersTest.java => test/org/mockito/usage/matchers/InvalidUseOfMatchersTest.java
    rename : test/org/easymock/MatchersTest.java => test/org/mockito/usage/matchers/MatchersTest.java
    rename : test/org/easymock/MatchersToStringTest.java => test/org/mockito/usage/matchers/MatchersToStringTest.java
    rename : test/org/mockito/usage/BasicStubbingTest.java => test/org/mockito/usage/stubbing/BasicStubbingTest.java
    rename : test/org/mockito/usage/ReturningDefaultValuesTest.java => test/org/mockito/usage/stubbing/ReturningDefaultValuesTest.java
    rename : test/org/mockito/usage/StubbingWithThrowablesTest.java => test/org/mockito/usage/stubbing/StubbingWithThrowablesTest.java
    rename : test/org/mockito/usage/BasicVerificationTest.java => test/org/mockito/usage/verification/BasicVerificationTest.java
    rename : test/org/mockito/usage/ExactNumberOfTimesVerificationTest.java => test/org/mockito/usage/verification/ExactNumberOfTimesVerificationTest.java
    rename : test/org/easymock/NiceMessagesWhenVerificationFailsTest.java => test/org/mockito/usage/verification/NiceMessagesWhenVerificationFailsTest.java
    rename : test/org/mockito/usage/NoMoreInteractionsVerificationTest.java => test/org/mockito/usage/verification/NoMoreInteractionsVerificationTest.java
    rename : test/org/mockito/usage/VerificationOnMultipleMocksUsingMatchersTest.java => test/org/mockito/usage/verification/VerificationOnMultipleMocksUsingMatchersTest.java
    rename : test/org/easymock/VerificationUsingMatchersTest.java => test/org/mockito/usage/verification/VerificationUsingMatchersTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%4020