commit 4c16e191e16941cf241a6f746fd27cbe6575bf5b
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Mon Aug 13 16:53:15 2012 +0100

    MDL-34862 question preview: improve preview ownership check.

    Users should only be able to access their own quetion preview. In the
    past, for reasons I can no longer remember, this was enforced
    using the session. It is much better to set the question_usage to belong
    to the user's context.