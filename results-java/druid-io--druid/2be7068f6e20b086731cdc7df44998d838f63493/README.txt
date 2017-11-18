commit 2be7068f6e20b086731cdc7df44998d838f63493
Author: Gian Merlino <gianmerlino@gmail.com>
Date:   Thu Jul 20 10:14:15 2017 -0700

    Fixes and improvements to SQL metadata caching. (#4551)

    * Fixes and improvements to SQL metadata caching.

    Also adds support for MultipleSpecificSegmentSpec to CachingClusteredClient.

    SQL changes:
    - Cache metadata on a per-segment level, in addition to per-dataSource, so
      we don't need to re-query all segments whenever a single new one appears.
      This should lower the load placed on the cluster by metadata queries.
    - Fix race condition in DruidSchema that can cause us to miss metadata. It was
      possible to notice new segments, then issue a query, and have that query
      not actually hit those segments, and not notice that it didn't hit those segments.
      Then, the metadata from those segments would be ignored.
    - Fix assumption in DruidSchema that all segments are immutable. Now, mutable
      segments are periodically re-queried.
    - Fix inappropriate re-use of SchemaPlus. Now we create one for each planning
      cycle, rather than sharing one. It caches table objects, which we want to
      avoid, since it can cause stale metadata. We do the caching in DruidSchema
      so we don't need the SchemaPlus caching.

    Server changes:
    - Add a TimelineCallback to TimelineServerView, for callers that want to get updates
      when the timeline has been modified.
    - Change CachingClusteredClient from a QueryRunner to a QuerySegmentWalker. This
      allows it to accept queries that are segment-descriptor-based rather than
      intervals-based. In particular it will now support MultipleSpecificSegmentSpec.

    * Fix DruidSchema, and unused imports.

    * Remove unused import.

    * Fix SqlBenchmark.