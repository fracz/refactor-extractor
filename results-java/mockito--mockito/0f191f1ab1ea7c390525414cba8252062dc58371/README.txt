commit 0f191f1ab1ea7c390525414cba8252062dc58371
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat May 2 16:25:12 2009 +0000

    Fixed some javadoc stuff
    Implemented runners so that they validate framework usage in tearDown()
    Made MockitoJUnitRunner working with JUnit 4.4 so that MockitoJUnit44Runner is no longer needed
    Some refactoring around Runners

    --HG--
    rename : src/org/mockito/internal/runners/LegacyJUnitRunner.java => src/org/mockito/internal/runners/MockitoJUnit44RunnerImpl.java
    rename : test/org/mockitousage/junitrunner/JUnit4RunnerTest.java => test/org/mockitousage/junitrunner/JUnit44RunnerTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%401391