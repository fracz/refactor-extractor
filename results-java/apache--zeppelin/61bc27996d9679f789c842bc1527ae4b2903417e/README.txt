commit 61bc27996d9679f789c842bc1527ae4b2903417e
Author: Renjith Kamath <renjith.kamath@gmail.com>
Date:   Fri Jan 22 09:43:19 2016 +0530

    ZEPPELIN-593 Test Paragraph action Disable run and refactor common methods

    ### What is this PR for?
    Add new selenium test for disable paragraph action and refactor some of the common methods which can be used by other tests.

    ### What type of PR is it?
    Improvement and refactor

    ### Is there a relevant Jira issue?
    ZEPPELIN-593

    ### How should this be tested?
    CI should pass

    or

    On OSX, you'll need firefox 42.0 installed, then you can run with
      `PATH=~/Applications/Firefox.app/Contents/MacOS/:$PATH CI="" mvn -Dtest=org.apache.zeppelin.integration.ParagraphActions -Denforcer.skip=true test -pl zeppelin-server`

    Author: Renjith Kamath <renjith.kamath@gmail.com>

    Closes #619 from r-kamath/ZEPPELIN-593 and squashes the following commits:

    008a381 [Renjith Kamath] fix typo and rename test
    5ce468a [Renjith Kamath] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into ZEPPELIN-593
    ccc76c1 [Renjith Kamath] ZEPPELIN-593 move common test functions to an abstract class
    93ab601 [Renjith Kamath] ZEPPELIN-593 Test Paragraph action Disable run and refactor common