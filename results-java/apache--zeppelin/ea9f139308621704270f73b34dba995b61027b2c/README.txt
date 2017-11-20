commit ea9f139308621704270f73b34dba995b61027b2c
Author: Ravi Ranjan <ranjanravi0308@gmail.com>
Date:   Wed Feb 24 11:04:20 2016 +0530

    Add new selenium test case to test clear output button

    ##What is this PR for?

    Add a new test case for testing the clear output button.
    Clear output button clears the output on the paragraph

    ##What type of PR is it?

    Test

    ##Is there a relevant Jira issue?

    NA

    ##How should this be tested?

    On OSX, you'll need firefox 42.0 installed, then you can run with

    PATH=~/Applications/Firefox.app/Contents/MacOS/:$PATH CI="" \
    mvn -Dtest=org.apache.zeppelin.ParagraphActionsIT -Denforcer.skip=true \
    test -pl zeppelin-server

    ##Questions:

    Does the licenses files need update?NO
    Is there breaking changes for older versions?NO
    Does this needs documentation?NO

    Author: Ravi Ranjan <ranjanravi0308@gmail.com>

    Closes #689 from ravicodder/testClearOutputButton and squashes the following commits:

    6348430 [Ravi Ranjan] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into testClearOutputButton
    60d2ea2 [Ravi Ranjan] Modified code to use reusable vriable to xpath
    f328061 [Ravi Ranjan] Correcting bad rebase
    88ddf31 [Ravi Ranjan] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into testClearOutputButton
    fcb42d3 [Ravi Ranjan] Use handleException
    4cdb6d4 [Ravi Ranjan] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into testClearOutputButton
    abb9673 [Ravi Ranjan] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into testClearOutputButton
    4e7ec99 [Ravi Ranjan]  Modifed the xpath and waitforparagraph till FINISHED state
    31e2849 [Ravi Ranjan] Merge remote-tracking branch 'origin/master' into testClearOutputButton
    3d9b601 [Ravi Ranjan] Add LOG.error message in catch statement
    3a99ac2 [Ravi Ranjan] Add LOG.error message in catch statement of test
    ba079ef [Ravi Ranjan] Debug message improved
    ccecb6c [Ravi Ranjan] Add new selenium test case to test clear output button
    a8be230 [Ravi Ranjan] add modified checkthat message
    f4a0883 [Ravi Ranjan] Add new selenium test case for clear output button of paragraph