commit d8de9e5d72e5225be3f8353323b550674b27db5f
Author: Adam Murdoch <adam@gradle.com>
Date:   Sun Apr 23 14:28:52 2017 +1000

    Changed the contract of `BuildOperationQueue` to allow build operations to add further operations to the queue as they run. The queue cannot be reused once all work as completed.

    Changed the implementation so that it arranges work in a queue, with workers taking work off the queue. The implementation does not use more than `max-workers` worker threads. Previously it would potentially starting a thread per operation, which would then block waiting for permission to start work. The implementation could be made more efficient with improvements to how it performs synchronisation and with better integration with the current state of the worker lease service.