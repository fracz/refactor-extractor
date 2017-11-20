commit 465c51a419347f89a7d73c8014b3b46f27f1d5b8
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Tue Oct 11 10:27:39 2016 +0800

    ZEPPELIN-335. Pig Interpreter

    ### What is this PR for?
    Based on #338 , I refactor most of pig interpreter. As I don't think the approach in #338 is the best approach. In #338, we use script `bin/pig` to launch pig script, it is different to control that job (hard to kill and get progress and stats info).  In this PR, I use pig api to launch pig script. Besides that I implement another interpreter type `%pig.query` to leverage the display system of zeppelin. For the details you can check `pig.md`

    ### What type of PR is it?
    [Feature]

    ### Todos
    * Syntax Highlight
    * new interpreter type `%pig.udf`, so that user can write pig udf in zeppelin directly and don't need to build udf jar manually.

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-335

    ### How should this be tested?
    Unit test is added and also manual test is done

    ### Screenshots (if appropriate)

    ![image](https://cloud.githubusercontent.com/assets/164491/18986649/54217b4c-8730-11e6-9e33-25f98a98a9b6.png)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>
    Author: Ali Bajwa <abajwa@hortonworks.com>
    Author: AhyoungRyu <ahyoungryu@apache.org>
    Author: Jeff Zhang <zjffdu@gmail.com>

    Closes #1476 from zjffdu/ZEPPELIN-335 and squashes the following commits:

    73a07f0 [Jeff Zhang] minor update
    a1b742b [Jeff Zhang] minor update on doc
    e858301 [Jeff Zhang] address comments
    c85a090 [Jeff Zhang] add license
    58b4b2f [Jeff Zhang] minor update of docs
    1ae7db2 [Jeff Zhang] Merge pull request #2 from AhyoungRyu/ZEPPELIN-335/docs
    fe014a7 [AhyoungRyu] Fix docs title in front matter
    df7a6db [AhyoungRyu] Add pig.md to dropdown menu
    5e2e222 [AhyoungRyu] Minor update for pig.md
    39f161a [Jeff Zhang] address comments
    05a3b9b [Jeff Zhang] add pig.md
    a09a7f7 [Jeff Zhang] refactor pig Interpreter
    c28beb5 [Ali Bajwa] Updated based on comments: 1. Documentation: added pig.md with interpreter documentation and added pig entry to index.md 2. Added test junit test based on passwd file parsing example here https://pig.apache.org/docs/r0.10.0/start.html#run 3. Removed author tag from comment (this was copied from shell interpreter https://github.com/apache/incubator-zeppelin/blob/master/shell/src/main/java/org/apache/zeppelin/shell/ShellInterpreter.java#L42) 4. Implemented cancel functionality 5. Display output stream in case of error
    2586336 [Ali Bajwa] exposed timeout and pig executable via interpreter and added comments
    7abad20 [Ali Bajwa] initial commit of pig interpreter