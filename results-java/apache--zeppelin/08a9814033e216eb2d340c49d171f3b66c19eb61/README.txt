commit 08a9814033e216eb2d340c49d171f3b66c19eb61
Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>
Date:   Sun Apr 30 00:15:19 2017 +0530

    [Zeppelin 2367] Hive JDBC proxy user option should be available even without kerberos

    ### What is this PR for?
    Hive JDBC proxy user option should be available generically.

    ### What type of PR is it?
    [Improvement]

    ### What is the Jira issue?
    * [Zeppelin 2367](https://issues.apache.org/jira/browse/ZEPPELIN-2367)

    ### How should this be tested?
    Enable Shiro authentication and set `zeppelin.jdbc.auth.type` as `SIMPLE` in the interpreter setting, and observe the connection string for the Hive.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need an update? N/A
    * Is there breaking changes for older versions? N/A
    * Does this needs documentation? Yes

    Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>

    Closes #2229 from prabhjyotsingh/ZEPPELIN-2367 and squashes the following commits:

    84b5e55b2 [Prabhjyot Singh] add logger.warn for hive and impersonation
    45c90a8e2 [Prabhjyot Singh] improve doc
    9fee9d2a9 [Prabhjyot Singh] replace hive with generic method
    a348e969a [Prabhjyot Singh] revert "zeppelin.jdbc.auth.kerberos.proxy.enable" behaviour
    e2bdbb2ad [Prabhjyot Singh] include e as inner exception
    c180f5ce2 [Prabhjyot Singh] Merge remote-tracking branch 'origin/master' into ZEPPELIN-2367
    1802b453f [Prabhjyot Singh] remove hive string from logger
    513987a28 [Prabhjyot Singh] apply genric logic to appendProxyUserToURL
    3fa2b1e98 [Prabhjyot Singh] change name to appendProxyUserToURL
    a75167415 [Prabhjyot Singh] Merge remote-tracking branch 'origin/master' into ZEPPELIN-2367
    4c382eefa [Prabhjyot Singh] log user details as well
    d51e770b2 [Prabhjyot Singh] add doc in jdbc.md
    01b18b9d2 [Prabhjyot Singh] add doc (reverted from commit ee8a6b524c481210486761032cb1f5fd6266bb54)
    40489c89d [Prabhjyot Singh] Merge remote-tracking branch 'origin/master' into ZEPPELIN-2367
    ee8a6b524 [Prabhjyot Singh] add doc
    8999d93ae [Prabhjyot Singh] ZEPPELIN-2367: Hive JDBC proxy user option should be avail even without kerberos