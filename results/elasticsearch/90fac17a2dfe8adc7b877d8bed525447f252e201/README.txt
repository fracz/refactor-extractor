commit 90fac17a2dfe8adc7b877d8bed525447f252e201
Author: Christoph Büscher <christoph@elastic.co>
Date:   Thu Aug 20 16:25:31 2015 +0200

    Query refactoring: MatchQueryBuilder

    This add equals, hashcode, read/write methods, separates toQuery and JSON parsing and adds tests.
    Also moving MatchQueryBuilder.Type to MatchQuery to MatchQuery, adding serialization and hashcode,
    equals there.

    Relates to #10217