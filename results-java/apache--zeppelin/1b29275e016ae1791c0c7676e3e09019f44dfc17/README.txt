commit 1b29275e016ae1791c0c7676e3e09019f44dfc17
Author: Ravi Ranjan <ranjanravi0308@gmail.com>
Date:   Wed Mar 9 10:39:22 2016 +0530

    Add selenium test case to test hide and show line number button of paragraph

    What is this PR for?

    Add a new test case for testing the show line number and hide line number button.
    These buttons are used to show line number in paragraph

    What type of PR is it?

    Test

    Is there a relevant Jira issue?

    NA

    How should this be tested?

    On OSX, you'll need firefox 42.0 installed, then you can run with

    PATH=~/Applications/Firefox.app/Contents/MacOS/:$PATH CI="" \
    mvn -Dtest=org.apache.zeppelin.ParagraphActionsIT -Denforcer.skip=true \
    test -pl zeppelin-server

    Questions:

    Does the licenses files need update?NO
    Is there breaking changes for older versions?NO
    Does this needs documentation?NO

    Author: Ravi Ranjan <ranjanravi0308@gmail.com>

    Closes #690 from ravicodder/testHideAndShowLineNumber and squashes the following commits:

    e02c939 [Ravi Ranjan] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into testHideAndShowLineNumber
    9f033d1 [Ravi Ranjan] Merge https://github.com/ravicodder/incubator-zeppelin into testHideAndShowLineNumber
    41b1999 [Ravi Ranjan] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into testHideAndShowLineNumber
    3c176ce [Ravi Ranjan] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into testHideAndShowLineNumber
    c38c635 [Ravi Ranjan] Used reusable variable to store xpath
    fd9979d [Ravi Ranjan] Correcting bad rebase
    4aeb3df [Ravi Ranjan] Use handleException
    7323a76 [Ravi Ranjan] Merge remote-tracking branch 'origin/master' into testHideAndShowLineNumber
    2299edb [Ravi Ranjan] Indented the code properly
    de835cd [Ravi Ranjan] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into testHideAndShowLineNumber
    4265616 [Ravi Ranjan] Modify ElementNotVisible Exception to Exception
    b32d163 [Ravi Ranjan]  Modify InterruptedException to Exception
    1031e49 [Ravi Ranjan] Merge branch 'master' of https://github.com/ravicodder/incubator-zeppelin into testHideAndShowLineNumber
    60a945c [Ravi Ranjan] Remove testTitle test case
    53d5d82 [Ravi Ranjan] Modified Error message
    128db1f [Ravi Ranjan] Merge branch 'master' into testHideAndShowLineNumber
    63edda8 [Ravi Ranjan] Add LOG.error message in catch statement
    575f5ef [Ravi Ranjan] Modify the Debug messages and checkthat statement is improved to make sense
    e11ff73 [Ravi Ranjan] Add new selenium test case to test show line number and Hide line number button
    f326fd2 [Ravi Ranjan] Add new selenium test case to test hide and show line number button