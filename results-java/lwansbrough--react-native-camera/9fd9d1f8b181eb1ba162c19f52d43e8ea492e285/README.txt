commit 9fd9d1f8b181eb1ba162c19f52d43e8ea492e285
Author: Nick Pomfret <npomfret@users.noreply.github.com>
Date:   Fri Mar 24 07:00:56 2017 +0000

    (android): android performance improvements (#644)

    * removed re-saving of output file
     * moved image processing onto an async task to allow camera to be used while processing is running