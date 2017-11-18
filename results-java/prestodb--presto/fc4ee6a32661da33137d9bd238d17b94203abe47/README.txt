commit fc4ee6a32661da33137d9bd238d17b94203abe47
Author: David Phillips <david@acz.org>
Date:   Fri Jul 24 17:31:29 2015 -0700

    Use JDBC batches for inserting Raptor shard metadata

    This improves performance, especially when there is substantial
    latency between Presto and the metadata database.