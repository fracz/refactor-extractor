commit cc2e843c004944469466024083cccbc7fbaecfc9
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Fri Feb 13 12:00:06 2015 +0100

    Uses import tool counts computer for rebuilding missing counts store

    This is the main purpose of this commit. However a couple of related
    things have also been made in here to improve performance around counts
    rebuilding and convenience in the code:

    - Parallelized some aspects of counts computer from the import tool.
      Basically the node label are counted and efficiently cached, so that
      the relationship counts can be processed sequentially, using the
      node-label cache. Relationship counting is parallelized into
      reading(single) and counting(multi). Each thread keeping local counts,
      aggregated in the end.
    - ProducerStep has been simplified. Less code, more generic and easier to
      extend.
    - Convenience around executing and supervising stages by the introduced
      ExecutorSupervisors.

    Localy measurements show 4x improvement from before and scalability should
    be linear. The limitation is the size of the node cache, which "should" be
    OK given that you generally run larger stores on larger machines. Looking
    ahead there will be multi-pass support, where parts of the store is
    processed in each pass, will be added to the staging framework to remove
    problems for huge stores on not-that-huge machines memory-wise.