commit 664a13a2f69498fdd4ce4c1a29d970d9fb40f882
Author: astroshim <hsshim@nflabs.com>
Date:   Thu Dec 10 00:19:49 2015 +0900

    Add job operation API

    This PR is related to https://issues.apache.org/jira/browse/ZEPPELIN-137.
    I added some notebook job operations.

    Here is the examples.
    * Get notebook job status.
    ```
    ]# curl -XGET http://121.156.59.2:8080/api/notebook/job/2B3Z5BXJA
    {"status":"OK","body":[{"id":"20151121-212654_766735423","status":"FINISHED","finished":"Tue Nov 24 14:21:40 KST 2015","started":"Tue Nov 24 14:21:39 KST 2015"},{"id":"20151121-212657_730976687","status":"FINISHED","finished":"Tue Nov 24 14:21:40 KST 2015","started":"Tue Nov 24 14:21:40 KST 2015"},{"id":"20151121-235508_752057578","status":"FINISHED","finished":"Tue Nov 24 14:21:40 KST 2015","started":"Tue Nov 24 14:21:40 KST 2015"},{"id":"20151121-235900_1491052248","status":"FINISHED","finished":"Tue Nov 24 14:21:41 KST 2015","started":"Tue Nov 24 14:21:40 KST 2015"},{"id":"20151121-235909_1520022794","status":"RUNNING","finished":"Tue Nov 24 02:53:51 KST 2015","started":"Tue Nov 24 14:21:41 KST 2015"}]}
    ```

    * Run notebook job.
    ```
    ]# curl -XDELETE http://121.156.59.2:8080/api/notebook/job/2B3Z5BXJA
    {"status":"ACCEPTED"}
    ```

    * Stop(Delete) notebook job.
    ```
    ]# curl -XDELETE http://121.156.59.2:8080/api/notebook/job/2B3Z5BXJA
    {"status":"ACCEPTED"}
    ```

    * Run requested paragraph job.
    ```
    ]# curl -XPOST http://121.156.59.2:8080/api/notebook/job/2B3Z5BXJA/20151121-212654_766735423
    {"status":"ACCEPTED"}
    ```

    * Stop(Delete) requested paragraph job.
    ```
    ]# curl -XDELETE http://121.156.59.2:8080/api/notebook/job/2B3Z5BXJA/20151121-212654_766735423
    {"status":"ACCEPTED"}
    ```

    Author: astroshim <hsshim@nflabs.com>
    Author: root <root@ktadm002.(none)>

    Closes #465 from astroshim/improve/addApis and squashes the following commits:

    c668ed9 [astroshim] update REST API docs
    a83289e [astroshim] fix restapi testcase(status code).
    d0afd18 [astroshim] fix http status code.
    775e7c8 [astroshim] update testcase.
    fe8d5eb [astroshim] check if note config null.
    dbd2d03 [astroshim] update note.java to find error.
    861f652 [astroshim] fix indent error.
    2989096 [astroshim] add generateParagraphsInfo method to Note.java
    cb99391 [astroshim] merge with master
    8002cd5 [astroshim] change restapi testcase to find build problem.
    5bb2c5e [astroshim] fix getCronJob response code.
    73f3b2b [astroshim] add cron job api's
    3e2ea3d [astroshim] update testcase.
    722f05b [astroshim] merge seperated job testcase.
    2bff6c9 [root] add job run and stop testcase.
    6103c42 [astroshim] fix LineLength build error.
    e5f7103 [astroshim] fix whitespace build error.
    7a7f993 [astroshim] fix indentation build error.
    6c1acf3 [astroshim] fix result message.
    1255674 [astroshim] add job operation api