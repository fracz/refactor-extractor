commit 3219218620e795769e6f65287f134b6a43e9c010
Author: Karup <karuppayyar@qubole.com>
Date:   Sat May 27 15:44:53 2017 +0530

    [ZEPPELIN-2591] Show user info in spark job description

    ### What is this PR for?
    Show user info in spark job description in spark UI.
    With this info on spark UI, we will be able to find which user's job took most time, which users job is currently consuming the resources etc

    ### What type of PR is it?
    improvement

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-2591

    ### How should this be tested?
    Run a spark job(scala, python, sql, R) , check the spark UI jobs page.
    The job should display the username in its description.

    ### Screenshots (if appropriate)
    <img width="1032" alt="screen shot 2017-05-27 at 15 41 30" src="https://cloud.githubusercontent.com/assets/5082742/26520572/00924702-42f3-11e7-8938-5a4b875d6c5d.png">
    Check the description column

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Karup <karuppayyar@qubole.com>

    Closes #2369 from karuppayya/ZEPPELIN-2591 and squashes the following commits:

    569f660 [Karup] code cleanup
    47cf5ed [Karup] Code clean up
    2cc9c08 [Karup] Add description to jobs
    22d850d [Karup] Populate user info