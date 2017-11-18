commit 0cf7708007b01faac5012d939f3c50db274f858d
Author: Jason Gustafson <jason@confluent.io>
Date:   Tue Sep 19 05:12:55 2017 +0100

    MINOR: Move request/response schemas to the corresponding object representation

    This refactor achieves the following:

    1. Breaks up the increasingly unmanageable `Protocol` class and moves schemas closer to their actual usage.
    2. Removes the need for redundant field identifiers maintained separately in `Protocol` and the respective request/response objects.
    3. Provides a better mechanism for sharing common fields between different schemas (e.g. topics, partitions, error codes, etc.).
    4. Adds convenience helpers to `Struct` for common patterns (such as setting a field only if it exists).

    Author: Jason Gustafson <jason@confluent.io>

    Reviewers: Ismael Juma <ismael@juma.me.uk>

    Closes #3813 from hachikuji/protocol-schema-refactor