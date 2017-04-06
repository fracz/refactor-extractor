commit f0f6a2c396f0723235e4a71a4c9cf229e159d08c
Author: Lee Hinman <lee@writequit.org>
Date:   Thu Nov 6 11:09:27 2014 +0100

    Refactor shard recovery from anonymous class to ShardRecoveryHandler

    Previously the bulk of our shard recovery code was in a 300-line
    anonymous class in `RecoverySource`. This made it difficult to find and
    more difficult to read.

    This factors out that code into a `ShardRecoveryHandler` class, adding
    javadocs for each function and phase of the recovery, as well as
    comments explaining some of the more esoteric functions performed during
    recovery.

    It's hoped that this will help more people understand Elasticsearch's
    recovery procedure.

    No *major* functionality has changed, only typo corrections, some minor
    allocation improvements and logging clarification changes.