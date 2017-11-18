commit 11b99c62a6335eac518f2073decfb5c1486df076
Author: Daz DeBoer <daz@gradle.com>
Date:   Sun May 28 17:06:01 2017 -0600

    Start included build execution pre-emptively, in parallel

    By kicking off included builds before their outputs are actually
    required, we can pre-emptively execute all tasks that are known
    to be required for the current invocation, in parallel.

    Any task requirements that are discovered during the course of
    task execution (e.g. compileOnly dependencies of transitive
    dependencies) will be executed in a subsequent included build
    invocation.

    Future work will improve this by determining more of the
    required included build tasks up-front when constructing
    the task graph of the including build.