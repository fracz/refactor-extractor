commit 620211800e434c097ef04b1b0663c16b6522b69b
Author: Christoph Büscher <christoph.buescher@elasticsearch.com>
Date:   Mon Apr 13 18:50:03 2015 +0200

    Query refactoring: BoostingQueryBuilder and Parser

    As part of the refactoring of queries this PR splits the parsers parse() method
    into a parsing and a query building part, adding serialization, hashCode() and
    equals() to the builder.

    Relates to #10217
    Closes #11621