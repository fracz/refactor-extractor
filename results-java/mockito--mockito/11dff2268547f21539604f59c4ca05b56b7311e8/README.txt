commit 11dff2268547f21539604f59c4ca05b56b7311e8
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Nov 16 22:56:30 2007 +0000

    -killed/refactored some tests
    -started purging EasyMock

    --HG--
    rename : src/org/easymock/internal/ObjectMethodsFilter.java => src/org/easymock/internal/MockitoObjectMethodsFilter.java
    rename : test/org/easymock/CompareToTest.java => test/org/easymock/ComparableMatchersTest.java
    rename : test/org/easymock/UsingMatchersTest.java => test/org/easymock/MatchersTest.java
    rename : test/org/easymock/ConstraintsToStringTest.java => test/org/easymock/MatchersToStringTest.java
    rename : test/org/easymock/NameTest.java => test/org/easymock/NiceMessagesWhenVerificationFailsTest.java
    rename : test/org/mockito/util/Matchers.java => test/org/mockito/util/JUnitMatchers.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%4018