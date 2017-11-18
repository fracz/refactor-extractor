commit a3f1d9cdfdd32c401bb76a020e3e4e5157212068
Author: Moxie Marlinspike <moxie@thoughtcrime.org>
Date:   Mon Nov 3 15:16:04 2014 -0800

    Beginning of libtextsecure refactor.

    1) Break out appropriate components.

    2) Switch the incoming pipeline from SendReceiveService to
       the JobManager.