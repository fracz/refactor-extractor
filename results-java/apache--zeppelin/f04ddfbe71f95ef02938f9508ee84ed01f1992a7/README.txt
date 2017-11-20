commit f04ddfbe71f95ef02938f9508ee84ed01f1992a7
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Sun Jun 25 10:08:12 2017 +0800

    ZEPPELIN-2615. Upgrade pig to 0.17.0 to support spark engine

    ### What is this PR for?

    Pig 0.17.0 has just been released. This PR is to upgrade pig to 0.17.0 and support spark engine which is a big milestone of pig 0.17.0

    Main Changes:

    * Upgrade pig to 0.17.0
    * Remove some code using java reflection in `PigUtils.java`, as pig 0.17.0 has some improvement and expose new apis which could be used pig interpreter.
    * Support spark engine

    ### What type of PR is it?
    [Improvement | Feature]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://github.com/zjffdu/zeppelin/tree/ZEPPELIN-2615

    ### How should this be tested?
    Unit test is added and also manually test spark yarn-client mode in pig tutorial note.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #2431 from zjffdu/ZEPPELIN-2615 and squashes the following commits:

    d4e9a6d [Jeff Zhang] Address comments
    4b4e3db [Jeff Zhang] ZEPPELIN-2615. Upgrade pig to 0.17.0 to support spark engine