commit c456967d494ec4c7e87f18e623bb671328fc7045
Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>
Date:   Mon Jun 27 14:13:40 2016 +0530

    [ZEPPELIN-1065] Flaky Test - ParagraphActionsIT.testRemoveButton

    ### What is this PR for?
    This is fix for fixing flaky CI failing test

    ```
    testRemoveButton(org.apache.zeppelin.integration.ParagraphActionsIT)  Time elapsed: 9.641 sec  <<< FAILURE!
    java.lang.AssertionError: After Remove : Number of paragraphs are
    Expected: <2>
         but: was <1>
            at org.hamcrest.MatcherAssert.assertThat(MatcherAssert.java:20)
            at org.junit.Assert.assertThat(Assert.java:865)
            at org.junit.rules.ErrorCollector$1.call(ErrorCollector.java:65)
            at org.junit.rules.ErrorCollector.checkSucceeds(ErrorCollector.java:78)
            at org.junit.rules.ErrorCollector.checkThat(ErrorCollector.java:63)
            at org.apache.zeppelin.integration.ParagraphActionsIT.testRemoveButton(ParagraphActionsIT.java:161)
    ```

    ### What type of PR is it?
    [Bug Fix]

    ### Todos
    * [x] - Add delay after paragraph delete.

    ### What is the Jira issue?
    * [ZEPPELIN-1065](https://issues.apache.org/jira/browse/ZEPPELIN-1065)

    ### How should this be tested?
    CI should be green

    ### Questions:
    * Does the licenses files need update? n/a
    * Is there breaking changes for older versions? n/a
    * Does this needs documentation? n/a

    Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>

    Closes #1091 from prabhjyotsingh/ZEPPELIN-1065 and squashes the following commits:

    f1b42b9 [Prabhjyot Singh] - refactor sleep into ZeppelinITUtils  - use clickAndWait before reading any WebElement
    c70709f [Prabhjyot Singh] add delay after deleting paragraph.