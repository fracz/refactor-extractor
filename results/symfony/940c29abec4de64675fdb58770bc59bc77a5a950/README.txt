commit 940c29abec4de64675fdb58770bc59bc77a5a950
Merge: f99dfb0 449b691
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 22 12:45:31 2017 -0700

    minor #21926 [Routing] Optimised dumped matcher (frankdejonge)

    This PR was squashed before being merged into the 3.3-dev branch (closes #21926).

    Discussion
    ----------

    [Routing] Optimised dumped matcher

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    TL;DR: I've optimised the PhpMatcherDumper output for a <del>60x</del> 4.4x performance improvement on a collection of ~800 routes by inducing cyclomatic complexity.

    [EDIT] The 60x performance boost was only visible when profiling with blackfire, which is quite possibly a result of the cost of profiling playing a part. After doing some more profiling the realistic benefit of the optimisation is more likely to be in the ranges is 1.3x to 4.4x.

    After the previous optimisation I began looking at how the PrefixCollection was adding its performance boost. I spotted another way to do this, which has the same theory behind it (excluding groups based on prefixes). The current implementation only groups when one prefix resides in the other. In this new implementation I've created a way to detect common prefixes, which allows for much more efficient grouping. Every time a route is added to the group it'll either merge into an existing group, merge into a new group with a route that has a common prefix, or merge into a new group with an existing group that has a common prefix.

    However, when a parameter is present grouping must only be done AFTER that route, this case is accounted for. In all other cases, where there's no collision routes can be grouped freely because if a group was matched other groups wouldn't have matched.

    After all the groups are created the groups are optimised. Groups with fewer than 3 children are inlined into the parent group. This is because a group with 2 children would potentially result in 3 prefix checks while if they are inlines it's 2 checks.

    Like with the previous optimisation I've profiled this using blackfire. But the match function didn't show up anymore. I've added `usleep` calls in the dumped matcher during profiling, which made it show up again. I've verified with @simensen that this is because the wall time of the function was too small for it to be of any interest. When it DID get detected, because of more tasks running, it would show up with around 250 nanoseconds. In comparison, the previous speed improvement brought the wall time down from 7ms to ~2.5ms on a set of ~800 routes.

    Because of the altered grouping behaviour I've not modified the PrefixCollection but I've created a new StaticPrefixCollection and updated the PhpMatcherDumper to use that instead.

    Commits
    -------

    449b6912dc [Routing] Optimised dumped matcher