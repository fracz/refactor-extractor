commit 620db7719ad3a7f46fb26c9737487cd8ef8a6bc1
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Tue Feb 26 00:03:23 2013 -0800

    HystrixCollapser Refactor for Performance

    IllegalStateException: Future Not Started https://github.com/Netflix/Hystrix/issues/80

    - refactored HystrixTimer to be multi-threaded and use ScheduledThreadPoolExecutor to achieve this
    - refactored HystrixCollapser to:
      -- timeout on wait and trigger batch execution from calling thread instead of relying on only background timer
      -- use single CountDownLatch per batch rather than 2 per collapsed requests to dramatically reduce number of items being waited on
      -- changed how batches are defined so the creation of a batch is a simpler step without a loop over all requests to register with the batch

    Testing on a production canary shows these changes have given significant efficiency and thus performance gains.