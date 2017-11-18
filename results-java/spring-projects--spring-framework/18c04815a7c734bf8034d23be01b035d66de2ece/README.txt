commit 18c04815a7c734bf8034d23be01b035d66de2ece
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Wed Feb 8 14:03:22 2017 +0100

    Add PathPatternRegistry

    This commit adds the new `PathPatternRegistry`, which  holds a
    sorted set of `PathPattern`s and allows for searching/adding patterns

    This registry is being used in `HandlerMapping` implementations and
    separates path pattern parsing/matching logic from the rest. Directly
    using `PathPattern` instances should improve the performance of those
    `HandlerMapping` implementations, since the parsing and generation of
    pattern variants (trailing slash, suffix patterns, etc) is done only
    once.

    Issue: SPR-14544