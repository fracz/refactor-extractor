commit f3e9f1e6e3d424709fcfd37977a2a3413023b933
Author: Andy Wilkinson <awilkinson@pivotal.io>
Date:   Wed Jun 1 17:02:32 2016 +0100

    Polish FileSystemWatcher and improve its thread safety

    - Limit shared state between FileSystemWatcher and the watching thread
    - Use a private monitor rather than synchronizing on this
    - Use a Runnable implementation rather than subclassing Thread
    - Synchronize consistently when reading and writing state

    Closes gh-6039