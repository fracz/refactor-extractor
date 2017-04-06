commit 0439abba8e64c1a22c66cc58672a3bcaf3ebc901
Merge: fef1546 d9af8e9
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Mon Dec 12 11:58:25 2016 +0100

    bug #20860 [WebProfilerBundle] Fix a web profiler form issue with fields added to the form after the form was built (tgalopin)

    This PR was merged into the 3.2 branch.

    Discussion
    ----------

    [WebProfilerBundle] Fix a web profiler form issue with fields added to the form after the form was built

    | Q             | A
    | ------------- | ---
    | Branch?       | 3.2
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #20823
    | License       | MIT
    | Doc PR        | -

    I discovered a bug in 3.2 in the web profiler and this PR fixes it. This issue has been fixed in the past by https://github.com/symfony/symfony/pull/13166 and probably reintroduced by the refactoring we did in the WebProfiler in 3.2. I simply applied the fix to these changes.

    To reproduce the original problem, simply clone the current standard edition, create a Form with a Type and add a field after its creation. Once done, try to access the Webprofiler: it will fails with the following error:

    > Key "type_class" for array with keys "id, name, view_vars, children" does not exist in @WebProfiler/Collector/form.html.twig at line 460.

    Commits
    -------

    d9af8e9 Fix a web profiler form issue with fields added to the form after the form was built