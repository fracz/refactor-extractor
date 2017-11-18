commit 5b7278764c89c1823156c4bb71350fb0a9bede9e
Author: Eric Hwang <ehwang@fb.com>
Date:   Fri Nov 22 19:08:53 2013 -0800

    Add support for range predicate pushdowns

    This work is a complete refactor of how we generate Partitions in the planning/execution phases.
    Instead of generating Partitions after planning, we now integrate the generation directly
    into the optimization planning phase so that we can iterate our plan based on data returned
    by the Partitions that we discover. This not only provides a number of opportunities for
    optimization, but also significantly simplifies the amount of cross-talk between the
    split generation and the planning phases. As a result of these changes, it makes
    it much easier to build native range predicate push downs.