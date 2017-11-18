commit 912f85793f4695368e9fa8666c01734319efd2b9
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Mon Jun 22 17:08:40 2015 +0200

    CheckPointer will use TransactionAppender for writing check points in the log

    In this way the log force needed by the check pointing can be batched together
    with several transaction commits.  This should improve performance and reduce
    contantion around log force.