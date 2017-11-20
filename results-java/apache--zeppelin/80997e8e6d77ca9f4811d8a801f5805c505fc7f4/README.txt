commit 80997e8e6d77ca9f4811d8a801f5805c505fc7f4
Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>
Date:   Wed Jul 20 14:04:26 2016 +0530

    [ZEPPELIN-1212] User impersonation support in JDBC

    ### What is this PR for?
    Add impersonation support to JDBC interpreters, in addition to Kerberos Authentication to improve auditability in all JDBC interpreters.

    ### What type of PR is it?
    [Bug Fix | Improvement]

    ### What is the Jira issue?
    * [ZEPPELIN-1212](https://issues.apache.org/jira/browse/ZEPPELIN-1212)

    ### How should this be tested?
    In JDBC interpreter setting add following properties

     - zeppelin.jdbc.auth.type = KERBEROS
     - zeppelin.jdbc.principal = principal value
     - zeppelin.jdbc.keytab.location = keytab location
     - enable shiro authentication via shiro.ini

    Now try and run any of hive's query (say show tables) it should return with valid results/errors depending on user permission.

    ### Questions:
    * Does the licenses files need update? n/a
    * Is there breaking changes for older versions? n/a
    * Does this needs documentation? n/a

    Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>

    Closes #1205 from prabhjyotsingh/ZEPPELIN-1212 and squashes the following commits:

    e22b681 [Prabhjyot Singh] Fix CI
    66824a0 [Prabhjyot Singh] ZEPPELIN-1212 User impersonation support in JDBC interpreter for Hive and Phoenix(Others)