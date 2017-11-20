commit 41f9fd921d77f5112107ba76c8794213cf3af929
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Thu Jan 12 15:39:17 2017 +0800

    ZEPPELIN-1933. Set pig job name and allow to set pig property in pig interpreter setting

    ### What is this PR for?
    Two improvements for pig interpreter.
    * Set job name via paragraph title if it exists, otherwise use the last line of pig script
    * Allow to set any pig property in interpreter setting

    ### What type of PR is it?
    [ Improvement]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-1933

    ### How should this be tested?
    Unit tested and manually tested.

    ### Screenshots (if appropriate)
    ![image](https://cloud.githubusercontent.com/assets/164491/21840291/a6af18b4-d817-11e6-9778-02e12ec02be1.png)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #1885 from zjffdu/ZEPPELIN-1933 and squashes the following commits:

    d2e1cd4 [Jeff Zhang] address comments
    9cee380 [Jeff Zhang] ZEPPELIN-1933. Set pig job name and allow to set pig property in pig interpreter setting