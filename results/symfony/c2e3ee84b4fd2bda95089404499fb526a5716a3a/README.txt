commit c2e3ee84b4fd2bda95089404499fb526a5716a3a
Merge: 71b8c39 16d1b35
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 24 13:56:29 2014 +0200

    feature #12000 [WebProfilerBundle] Show AJAX requests in the symfony profiler toolbar (Burgov, fabpot, stof)

    This PR was merged into the 2.6-dev branch.

    Discussion
    ----------

    [WebProfilerBundle] Show AJAX requests in the symfony profiler toolbar

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Adds AJAX requests in the web debug toolbar.
    See #8896 for the original discussion.

    ![image](https://cloud.githubusercontent.com/assets/47313/4384087/43d1feb2-43b0-11e4-99c9-3e50e19e623f.png)

    Commits
    -------

    16d1b35 optimized JS for the AJAX section of the toolbar
    2e708d7 made minor tweaks to JS code
    8e4c603 replaced the AJAX icon with a smaller one
    b66f39a removed hack
    9c74fcc removed uneeded web_profiler.debug_toolbar.excluded_ajax_paths parameter in the container
    d43edaf [WebProfilerBundle] improved the ajax section of the WDT
    37f7dd7 [WebProfilerBundle] Show AJAX requests in the symfony profiler toolbar