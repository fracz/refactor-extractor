commit 5c6599e1c24d23389a879baf8b59dae9017794e0
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Tue Jul 23 17:00:16 2013 -0700

    Reduce logging

    - change logger.error to logger.debug on noisy logs
    - improve wording on message where a command has failed and the fallback failed
    - logger.error is not needed since everywhere that it would be valuable throws a HystrixRuntimeException which will be handled or logged elsewhere