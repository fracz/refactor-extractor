commit 9952454f4efc6a8734149b5e81e4f66c0349dcbb
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Dec 11 21:02:04 2007 +0000

    package refactoring + changing names

    --HG--
    rename : src/org/mockito/internal/MockAwareInvocationHandler.java => src/org/mockito/internal/creation/MockAwareInvocationHandler.java
    rename : src/org/mockito/internal/MockFactory.java => src/org/mockito/internal/creation/MockFactory.java
    rename : src/org/mockito/internal/ObjectMethodsFilter.java => src/org/mockito/internal/creation/ObjectMethodsFilter.java
    rename : src/org/mockito/internal/ObjenesisClassInstantiator.java => src/org/mockito/internal/creation/ObjenesisClassInstantiator.java
    rename : src/org/mockito/internal/Invocation.java => src/org/mockito/internal/invocation/Invocation.java
    rename : src/org/mockito/internal/InvocationChunk.java => src/org/mockito/internal/invocation/InvocationChunk.java
    rename : src/org/mockito/internal/InvocationMatcher.java => src/org/mockito/internal/invocation/InvocationMatcher.java
    rename : src/org/mockito/internal/InvocationsFinder.java => src/org/mockito/internal/invocation/InvocationsFinder.java
    rename : src/org/mockito/internal/MatchersBinder.java => src/org/mockito/internal/invocation/MatchersBinder.java
    rename : src/org/mockito/internal/LastArguments.java => src/org/mockito/internal/state/LastArguments.java
    rename : src/org/mockito/internal/MockitoState.java => src/org/mockito/internal/state/MockitoState.java
    rename : src/org/mockito/internal/MockitoStateImpl.java => src/org/mockito/internal/state/MockitoStateImpl.java
    rename : src/org/mockito/internal/ThreadSafeMockitoState.java => src/org/mockito/internal/state/ThreadSafeMockitoState.java
    rename : src/org/mockito/internal/EmptyReturnValues.java => src/org/mockito/internal/stubbing/EmptyReturnValues.java
    rename : src/org/mockito/internal/IAnswer.java => src/org/mockito/internal/stubbing/IAnswer.java
    rename : src/org/mockito/internal/Result.java => src/org/mockito/internal/stubbing/Result.java
    rename : src/org/mockito/internal/StubbedInvocationMatcher.java => src/org/mockito/internal/stubbing/StubbedInvocationMatcher.java
    rename : src/org/mockito/internal/Stubber.java => src/org/mockito/internal/stubbing/Stubber.java
    rename : src/org/mockito/internal/MissingInvocationVerifier.java => src/org/mockito/internal/verification/MissingInvocationVerifier.java
    rename : src/org/mockito/internal/NumberOfInvocationsVerifier.java => src/org/mockito/internal/verification/NumberOfInvocationsVerifier.java
    rename : src/org/mockito/internal/OrderOfInvocationsVerifier.java => src/org/mockito/internal/verification/OrderOfInvocationsVerifier.java
    rename : src/org/mockito/internal/Verifier.java => src/org/mockito/internal/verification/Verifier.java
    rename : src/org/mockito/internal/VerifyingRecorder.java => src/org/mockito/internal/verification/VerifyingRecorder.java
    rename : test/org/mockito/internal/CglibTest.java => test/org/mockito/internal/creation/CglibTest.java
    rename : test/org/mockito/internal/MockFactoryTest.java => test/org/mockito/internal/creation/MockFactoryTest.java
    rename : test/org/mockito/internal/InvocationBuilder.java => test/org/mockito/internal/invocation/InvocationBuilder.java
    rename : test/org/mockito/internal/InvocationChunkTest.java => test/org/mockito/internal/invocation/InvocationChunkTest.java
    rename : test/org/mockito/internal/InvocationMatcherTest.java => test/org/mockito/internal/invocation/InvocationMatcherTest.java
    rename : test/org/mockito/InvocationTest.java => test/org/mockito/internal/invocation/InvocationTest.java
    rename : test/org/mockito/internal/MockitoStateImplTest.java => test/org/mockito/internal/state/MockitoStateImplTest.java
    rename : test/org/mockito/internal/EmptyReturnValuesTest.java => test/org/mockito/internal/stubbing/EmptyReturnValuesTest.java
    rename : test/org/mockito/internal/StubberTest.java => test/org/mockito/internal/stubbing/StubberTest.java
    rename : test/org/mockito/internal/NumberOfInvocationsVerifierTest.java => test/org/mockito/internal/verification/NumberOfInvocationsVerifierTest.java
    rename : test/org/mockito/internal/RegisteredInvocationsTest.java => test/org/mockito/internal/verification/RegisteredInvocationsTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40163