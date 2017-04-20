commit 47ae8654281874b7e30859876bc05d984d3b8d68
Author: Ben Newman <bn@cs.stanford.edu>
Date:   Sun Feb 16 12:54:40 2014 -0500

    Make ReactWebWorker-test.js less flaky.

    Two improvements: make sure we set `done = true` when an error message is
    received, so that the test output contains the error message instead of
    eventually timing out and displaying nothing useful; and increase the
    timeout for this particular test from 5000ms (the default) to 20000ms.