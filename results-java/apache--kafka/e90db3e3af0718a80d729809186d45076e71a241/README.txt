commit e90db3e3af0718a80d729809186d45076e71a241
Author: Guozhang Wang <wangguoz@gmail.com>
Date:   Fri Jan 13 11:28:40 2017 -0800

    MINOR: Remove unnecessary store info from TopologyBuilder

    This PR is extracted from https://github.com/apache/kafka/pull/2333 as an incremental fix to ease the reviewing:

    1. Removed `storeToProcessorNodeMap` from ProcessorTopology since it was previously used to set the context current record, and can now be replaced with the dirty entry in the named cache.

    2. Replaced `sourceStoreToSourceTopic` from ProcessorTopology with `storeToChangelogTopic` map, which includes the corresponding changelog topic name for all stores that are changelog enabled.

    3. Modified `ProcessorStateManager` to rely on `sourceStoreToSourceTopic` when retrieving the changelog topic; this makes the second parameter `loggingEnabled` in `register` not needed any more, and we can deprecate the old API with a new one.

    4. Also fixed a minor issue in `KStreamBuilder`: if the storeName is not provided in the `table(..)` function, do not create the underlying materialized store. Modified the unit tests to cover this case.

    5. Fixed a bunch of other unit tests failures that are exposed by this refactoring, in which we are not setting the applicationId correctly when constructing the mocking processor topology.

    Author: Guozhang Wang <wangguoz@gmail.com>

    Reviewers: Damian Guy, Matthias J. Sax, Ewen Cheslack-Postava

    Closes #2338 from guozhangwang/KMinor-refactor-state-to-changelogtopic