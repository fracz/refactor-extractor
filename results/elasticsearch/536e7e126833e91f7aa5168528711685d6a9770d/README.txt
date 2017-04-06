commit 536e7e126833e91f7aa5168528711685d6a9770d
Author: Simon Willnauer <simonw@apache.org>
Date:   Fri Dec 4 09:43:32 2015 +0100

    Remove ancient deprecated and alternative recovery settings

    Several settings have been deprecated or are replaced with new settings after refactorings
    in version 1.x. This commit removes the support for these settings.

    The settings are:

     * `index.shard.recovery.translog_size`
     * `index.shard.recovery.translog_ops`
     * `index.shard.recovery.file_chunk_size`
     * `index.shard.recovery.concurrent_streams`
     * `index.shard.recovery.concurrent_small_file_streams`
     * `indices.recovery.max_size_per_sec`