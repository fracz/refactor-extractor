commit cb6024c480744e6d340bc93f25bde3ff5e14d8b9
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Dec 2 11:13:20 2013 +0100

    Status / progress bar uses events hierarchy to render the progress

    1. This improves the parallel build behavior. It is still not completely pretty when parallel build runs because we're missing the Building 23% progress info. The fix for this comes next.
    2. If the parent was not registered yet or completed, this scenario is tolerated currently. It needs to be revisited when we decide on the strictness of the general algorithm.