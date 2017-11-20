commit a5b437d7fe5adcdb4546b91af5f77b0252d2fd64
Author: purechoc <purechoc.en@gmail.com>
Date:   Thu Dec 29 09:08:18 2016 +0900

    [ZEPPELIN-1609] using pyspark(python3) with livy interperter

    ### What is this PR for?
    using pyspark(python3) with livy interperter

    ### What type of PR is it?
    Improvement

    ### Todos

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1609

    ### How should this be tested?
    %livy.pyspark3
    3/2
    check result is "1.5"

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? - NO
    * Is there breaking changes for older versions?  - NO
    * Does this needs documentation?  - NO

    Author: purechoc <purechoc.en@gmail.com>
    Author: purechoc <purechoc@ncsoft.com>

    Closes #1587 from purechoc/zeppelin-livy-pyspark3 and squashes the following commits:

    6f7391f [purechoc] update template
    7c0f70d [purechoc] refactor code
    476ae25 [purechoc] Merge remote-tracking branch 'upstream3/master' into zeppelin-livy-pyspark3
    55d486f [purechoc] Trigger
    e5db640 [purechoc] add properties
    51140f2 [purechoc] Trigger
    3fc98cd [purechoc] add configuration
    d0e2293 [purechoc] Trigger
    7e42108 [purechoc] add %livy.python3
    3775e47 [purechoc] add %livy.python3
    c0babf6 [purechoc] add %livy.python3