commit 880e37be37ebffba7c144afdb4617319b4a308b2
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon May 4 18:35:30 2009 +0000

    refactored code around MockHandler - moved all the inner classes outside
    enabled spy/partial mock tests as the functionality is working now

    --HG--
    rename : src/org/mockito/internal/stubbing/AnswersValidator.java => src/org/mockito/internal/stubbing/answers/AnswersValidator.java
    rename : src/org/mockito/internal/stubbing/CallsRealMethod.java => src/org/mockito/internal/stubbing/answers/CallsRealMethod.java
    rename : src/org/mockito/internal/stubbing/DoesNothing.java => src/org/mockito/internal/stubbing/answers/DoesNothing.java
    rename : src/org/mockito/internal/stubbing/Returns.java => src/org/mockito/internal/stubbing/answers/Returns.java
    rename : src/org/mockito/internal/stubbing/ThrowsException.java => src/org/mockito/internal/stubbing/answers/ThrowsException.java
    rename : test/org/mockito/internal/stubbing/AnswersValidatorTest.java => test/org/mockito/internal/stubbing/answers/AnswersValidatorTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%401408