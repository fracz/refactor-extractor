commit 661f8a967f320fa4f7021d148bd52319342f3f9e
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Wed Nov 7 09:09:18 2012 +0100

    reuse cached Zinc compiler rather than recreating it all the time

    - vastly improves performance for multi-project builds