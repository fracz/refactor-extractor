commit a0887224bb05242e49be45da0096f1c5954b5aa9
Merge: 0876a19 c7a8f7a
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Nov 12 10:36:54 2012 +0100

    merged branch arnaud-lb/apache-dumper (PR #5792)

    This PR was merged into the master branch.

    Commits
    -------

    c7a8f7a [Routing] fixed possible parameters conflict in apache url matcher

    Discussion
    ----------

    [Routing] fixed possible parameters conflict in apache url matcher

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no (as long as rewrite rules are generated after upgrading)
    Symfony2 tests pass: yes

    - This fixes a conflict in route parameters:

      The rewrite rules currently pass route informations through environment variables:

      `_ROUTING_DEFAULT_x`: passes the default value of parameter x
      `_ROUTING__allow_x`: passes the information that method x was allowed for this route
      `_ROUTING_x`: passes the value of parameter x

      The problem is that naming a route parameter `DEFAULT_*` or `_allow_*` would not behave as expected.

      I fixed this by namespacing all environment variables; e.g. parameters are in `_ROUTING_param_*`, defaults in `_ROUTING_default_*`, etc.

    - The PR fixes a second issue: sometimes the variables are prefixed with multiple REDIRECT_. This PR handles this case by ignoring them all.

    - This also improves performance a little:

      Matching a route with two parameters and two default parameters 100K times: (`$_SERVER` was copied from a real request, so with many non `_ROUTING_` variables)
      master: 6.6s
      this branch: 4.7s

    ---------------------------------------------------------------------------

    by fabpot at 2012-10-27T13:37:24Z

    Any news on this PR? Is it mergeable?

    ---------------------------------------------------------------------------

    by arnaud-lb at 2012-10-27T14:50:08Z

    There is an issue with default parameter values, I can't find how to fix that in a simple way. Before this PR, default values are never used (if a parameter is an optional not present in the url, the parameter's value is the empty string); after this PR, when a parameter is present and empty (e.g. a requirement like `.*`), its value is set to its default value.

    ---------------------------------------------------------------------------

    by Tobion at 2012-10-29T01:36:08Z

    The problem is, it's not consistent with the default php matcher. So one cannot safely exchange it with the apache matcher because it behaves differently under some (special) circumstances.

    ---------------------------------------------------------------------------

    by fabpot at 2012-11-05T08:05:54Z

    We need to move forward as I want to merge the hostname support in the routing ASAP to have plenty of time for feedback before the 2.2 release.

    Does it sound reasonable to merge this PR as is an open a ticket about the remaining issue (which should not occur that often anyways)?

    ---------------------------------------------------------------------------

    by arnaud-lb at 2012-11-05T09:22:02Z

    @fabpot it sounds reasonable to me. Also, I've the hostname support branch is currently rebased so that it can be merged without this one.

    ---------------------------------------------------------------------------

    by Tobion at 2012-11-11T21:50:20Z

    Btw, does the ApacheMatcherDumper handle the _scheme requirement? It doesn't look like it. This would be another bug.
    Anyway, we can probably merge this PR and open new issues for the remaining bugs.