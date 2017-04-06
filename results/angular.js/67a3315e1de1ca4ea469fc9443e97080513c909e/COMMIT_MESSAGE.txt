commit 67a3315e1de1ca4ea469fc9443e97080513c909e
Author: DiPeng <pengdi@go.wustl.edu>
Date:   Mon Aug 1 22:15:33 2011 -0700

    refactor(angular): remove unnecessary parameter for slice function

    - the end index for slice, if not specified, is default to the
    end of the array it operates on.