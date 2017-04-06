commit 162ad1251cca0e26121d3db1a989a2bb6ba50749
Author: Simon Willnauer <simon.willnauer@elasticsearch.com>
Date:   Sat Aug 27 21:42:38 2016 +0200

    Fsync documents in an async fashion (#20145)

    today we fsync in a blocking fashion where all threads block while another
    syncs. Yet, we can improve this and make use of the async infrastrucutre added
    for `wait_for_refresh` and make fsyncing single threaded while all other threads
    can continue indexing. The syncing thread then notifies a listener once the requests
    location is synced. This also allows to send docs to replicas before its actually fsynced
    allowing for cocurrent replica processing.

    This patch has a significant impact on performance on slower discs. An initial single node benchmark
    shows that on very fast SSDs there is no noticable impact but on slow spinning disk this
    patch shows a ~32% performance improvement.

    ```
    NVME SSD:

    336ec0ac9a12b967163a4a21f75beb41c8582cde (master):

     Total docs/sec: 47200.9
     Total docs/sec: 46440.4

    23543a97e3e7f72a31e26b50e00931919784426c (async wait for translog):

     Total docs/sec: 47461.6
     Total docs/sec: 46188.3
    -------------------------------------------------------------------
    Spinning disk:

    336ec0ac9a12b967163a4a21f75beb41c8582cde (master):

     Total docs/sec: 22733.0
     Total docs/sec: 24129.8

    23543a97e3e7f72a31e26b50e00931919784426c (async wait for translog):

     Total docs/sec: 32724.1
     Total docs/sec: 32845.4
    --------------------------------------------------------------------
    ```