commit ffaac5a08a116157c6e939dee3ea792450ef1c1e
Author: Tim Brooks <tim@uncontended.net>
Date:   Thu Apr 13 14:48:52 2017 -0500

    Simplify BulkProcessor handling and retry logic (#24051)

    This commit collapses the SyncBulkRequestHandler and
    AsyncBulkRequestHandler into a single BulkRequestHandler. The new
    handler executes a bulk request and awaits for the completion if the
    BulkProcessor was configured with a concurrentRequests setting of 0.
    Otherwise the execution happens asynchronously.

    As part of this change the Retry class has been refactored.
    withSyncBackoff and withAsyncBackoff have been replaced with two
    versions of withBackoff. One method takes a listener that will be
    called on completion. The other method returns a future that will been
    complete on request completion.