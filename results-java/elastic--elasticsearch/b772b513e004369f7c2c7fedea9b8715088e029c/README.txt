commit b772b513e004369f7c2c7fedea9b8715088e029c
Author: Simon Willnauer <simonw@apache.org>
Date:   Tue Oct 20 15:02:06 2015 +0200

    Refactor ShardFailure listener infrastructure

    Today we leak the notion of an engine outside of the shard abstraction
    which is not desirable. This commit refactors the infrastrucutre to use
    use already existing interfaces to communicate if a shard has failed and
    prevents engine private classes to be implemented on a higher level.
    This change is purely cosmentical...