commit 35f416d4de22e8490e6a6d6c3f58477a80c6a9d0
Author: lutovich <konstantin.lutovich@neotechnology.com>
Date:   Mon Feb 1 11:41:30 2016 +0100

    Better lucene index writer config for index population

    This commit makes IndexWriterConfig configurable when LuceneSchemaIndex is
    created. Then during population an improved version of the config is used. It
    disables flushing by number of documents and enables flushing by RAM buffer
    size. Value of this size is also increased from default 16M to 50M so flushes
    of newly added documents happen less frequently.

    Each IndexWriterConfig is attached to a single IndexWriter and can't be
    reused to create other writers. That is why Supplier<IndexWriterConfig>
    is used everywhere until actual writer creation.