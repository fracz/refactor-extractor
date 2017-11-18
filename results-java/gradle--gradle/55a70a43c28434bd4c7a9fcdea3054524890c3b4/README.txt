commit 55a70a43c28434bd4c7a9fcdea3054524890c3b4
Author: Marcin Erdmann <erdi84@gmail.com>
Date:   Wed Mar 18 16:04:08 2015 +0000

    Revert "Add lazy initialization of source uri to UriResource because doing it eagerly shows up in the profiler for many empty projects help benchmark and that field is not accessed for that benchmark."
    That optimization did not bring any performance improvements due to source uris of all UriResources being evaluated anyway.

    This reverts commit 510984733f311cf09ceb5fc24207fa649e48321a.

    +review REVIEW-5441