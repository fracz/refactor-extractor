commit 53c4859a6a20a2484ca19abc239716eb1e3e410e
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Fri Aug 29 14:58:01 2014 +0200

    Reuse data source validation query in health endpoint

    This commit improves DataSourceMetadata to expose the validation
    query. This can be used by DataSourceHealthIndicator as the query
    to use instead of "guessing" which query could be applied according
    to the database type.

    Fixes gh-1282