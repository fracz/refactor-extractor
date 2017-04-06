commit 283fe90e7a5e734be14967f569db1ea21e7d8846
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed May 13 18:43:09 2015 +0200

    Query Refactoring: Moving parser NAME constant to corresponding query builder

    As part of the query refactoring, this PR moves the NAME field that currently identifies each query parser to the
    corresponding query builder. The reason for this, as discussed in #11121 (comment), is that we need still need to
    be able to link parser and builder implementations, but that the query builder is independent of the parser (queries
    don't necessarily need to be coverted to XContent any more). Builders don't need to know about their parsers, but
    parsers need to be linked to their respective builders.

    Closes #11178