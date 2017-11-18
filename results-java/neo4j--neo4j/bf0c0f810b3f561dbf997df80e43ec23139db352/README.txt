commit bf0c0f810b3f561dbf997df80e43ec23139db352
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Tue Aug 23 20:18:20 2016 +0200

    Revamped parallelism of parts of batch importer

    The main story here is composed of two things:

    - a new ForkedProcessorStep which does parallelization inside each
      batch, executed one by one. This to avoid difficulties parallelizing
      some steps which has a costly section which isn't parallelizable.
      With this new step items in a batch can be striped such that each
      forked processor knows which parts to process.
    - better mechanical sympathy where most stages are optimized to work
      with batch sizes matching pages in the page cache of the store they
      (mainly) work with.

    The forked processor simplifies a couple of stages, there are now
    no artificial additional steps for splitting or otherwise modify
    batches to be better parallelizable. Also the whole stage scales
    better with added processors because the old way of parallelizing
    those stages often involved a step which was single-threaded and
    acted as a divider-of-work. Such a step would often become the
    bottleneck in the end anyway.

    About mechanical sympathy the main problem previously was that reader
    and writer of stages which read from and wrote to the same store actually
    contended on each other. Given the smaller batch size, there were
    multiple batches of read records for any given page. Later in the stage
    where store was updated would often update the same page and so the
    reader (still reading that page) would need to do mych more retry-
    reading and so slow the whole stage down. Now with the aligned
    batch sizes the reader doesn't contend with the writers in the
    page cache.

    Additionally the main store updating step have been split into steps
    updating entities and properties separately, this to have the
    entity updating able to go even faster.

    The net result of this change as a whole should be that more often
    the disk is the only main bottleneck. On test machines and development
    laptops a 2x-3x performance improvement of the importer have
    been observed.