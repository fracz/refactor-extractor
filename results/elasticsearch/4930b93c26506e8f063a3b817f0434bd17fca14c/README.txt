commit 4930b93c26506e8f063a3b817f0434bd17fca14c
Author: Shay Banon <kimchy@gmail.com>
Date:   Tue Jul 23 00:08:45 2013 +0200

    move master actions to accept a lister, improve cluster service state execution
    - master actions many times end up being executed on the cluster service, so there is no need to block them on the management thread pool to wait for a response, this remove the load on the management thread pool, and also simplifies the code implementing it
    - cluster service state update exception handling was improved to include a callback when a failure happens during state update execution, this makes sure that we catch all relevant exceptions and invoke the callback, as well as simplify the code of cluster state update tasks, as they can throw failures from within the execute method and then handle them properly