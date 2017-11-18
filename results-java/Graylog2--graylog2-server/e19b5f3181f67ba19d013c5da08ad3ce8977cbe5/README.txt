commit e19b5f3181f67ba19d013c5da08ad3ce8977cbe5
Author: Edmundo Alvarez <github@edmundoa.com>
Date:   Thu Nov 10 12:19:59 2016 +0100

    Enhance alert data model (#3072)

    * Add resolvedAt field to Alert model

    * Use MongoDB Date to store Alert DateTimes

    After the refactoring in 8b15f35f5fef46b0c6b64ec7488e71019708696f, new
    Alert documents were using Strings instead of Dates to store
    `triggered_at`. That change breaks the sorting of documents by that
    field, sorting that we use in some places of the code.

    This commit ensures that `triggered_at` and the newly added
    `resolved_at` fields are persisted as Date instances, keeping
    consistency with the data already stored in the collection.

    * Add is_interval field to Alert

    We need a way to differentiate alerts spanning through an interval of
    time and the old alerts that happened in a single point in time.

    * Adapt API DOA to latest changes in Alert model

    * Simplify Alert and AlertService interfaces

    * Add support for resolving alerts

    * Add resolve alert logic to AlertScanner

    * Use Date object to query Alerts by triggered_at

    Otherwise MongoJack throws a CodecConfigurationException when trying to
    convert the DateTime object.

    * Add some tests for loadRecentOfStream

    * Actually honor limit parameter on listAll

    Before we used the limit on each stream, so the returning results could
    be _limit * number of streams_.

    * Honor limit parameter for real

    The previous fix was still executing a MongoDB query per stream. The
    whole resource was only available for users with permission to read all
    streams.

    This commit changes that, so it will query MongoDB for alerts in the
    streams the user has read permissions. The query is only executed once,
    so limit parameter is also working as expected.

    * Clarify when resolvedAtDate can be null

    * Do not create an Alert on resolveAlert

    Use `toBuilder()` instead.