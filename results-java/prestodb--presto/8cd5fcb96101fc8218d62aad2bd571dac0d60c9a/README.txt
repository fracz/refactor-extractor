commit 8cd5fcb96101fc8218d62aad2bd571dac0d60c9a
Author: Eric Hwang <ehwang@fb.com>
Date:   Wed Jul 17 18:58:54 2013 -0700

    Add full predicate move around

    Also includes:
    - Expression equality inference (instead of only on symbols). This means we can replace parts of expressions to move predicates around.
    - Smart(er) selection of expression canonicalization when moving predicates around
    - Join clause optimizations (for inner join)
    - Predicate pull up
    - Complete refactored predicate pushdown for joins

    Future work:
    - Expression normalization (math and predicates)
    - Ability to transform expressions via multiple rewrites
    - More precise Expression cost estimator for EqualityInference
    - Join clause optimizations for outer joins