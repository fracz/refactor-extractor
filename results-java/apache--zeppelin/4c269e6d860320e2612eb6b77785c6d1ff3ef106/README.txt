commit 4c269e6d860320e2612eb6b77785c6d1ff3ef106
Author: Alexander Bezzubov <bzz@apache.org>
Date:   Tue Dec 22 13:07:43 2015 +0900

    ZEPPELIN-312: fix a bug with blocking websocket broadcast

    ### What is this PR for?
    Replacing synchronization through critical section over the collection of sockets with the lock-free collection implementation  `java.util.concurrent.ConcurrentLinkedQueue`.
    Synchronization was used to avoid parallel collection modifications, as the calls `.sendMessage()` in Jetty implementation of Websockets are thread-safe and can proceed concurrently.

    ### What type of PR is it?
    Bug Fix

    ### Is there a relevant Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-312

    ### How should this be tested?
    See JIRA

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Alexander Bezzubov <bzz@apache.org>

    Closes #558 from bzz/fix/zeppelin-312-blocking-broadcast and squashes the following commits:

    bbbf8ae [Alexander Bezzubov] ZEPPELIN-312: refactoring ZeppelinServer to better Java style naming conventions
    497a6ca [Alexander Bezzubov] ZEPPELIN-312: replace sync \w lock-free collection
    524c401 [Alexander Bezzubov] ZEPPELIN-312: refactoring ZeppelinServer to adhere Java naming conventions