commit 055234c893ea9402fe7eb1af742ef53f31670818
Merge: b10db19 6ddf9ae
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed Mar 16 16:29:10 2016 +0100

    Merge branch 'feature-suggest-refactoring'

    Refactors all suggestion builders to be able to be parsed on
    the coordinating node and serialized as objects to the shards.
    Specifically, all SuggestionBuilder implementations implement
    NamedWritable for serialization, a fromXContent() method that
    handles parsing xContent and a build() method that is called
    on the shard to create the SuggestionContext.

    Relates to #10217