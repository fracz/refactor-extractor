commit f4dd3711d1164ce6a442306615cac99119022215
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu May 11 17:25:23 2017 -0700

    Work on issue #38242094: Activity manager oom adj computation seems broken

    This shouldn't change any behavior, but improve the reason we
    report for each process's oom_adj / proc state level.  There are
    two significant things going on here:

    1. We now consider a bump up in proc state to be just as significant
    as a change in oom_adj, and accordingly update the reason when this
    happens.  Given how many proc stats we now have mapped to some of
    the single oom_adj levels, this matches your expectation to have the
    reason be why it is at that proc state and not just some random
    other thing at that level.

    2. There is special handling of an app at the top state connecting
    to another app, deciding the actual state to apply at the end.  This
    was not at that point updating the reason, so anything the top app
    is connected to would get the top state but myseriously have some other
    reason, looking very broken.  We now propagate the reason over.

    Test: manual

    Change-Id: Idecbe206d73e7c4cbd989ef6faf3b1679e06c088