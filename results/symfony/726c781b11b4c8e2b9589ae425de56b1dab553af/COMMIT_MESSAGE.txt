commit 726c781b11b4c8e2b9589ae425de56b1dab553af
Merge: d5624a6 ebc23cf
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Tue Nov 29 11:33:09 2016 +0100

    bug #20679 [VarDumper] Use default color for ellipsed namespaces/paths (nicolas-grekas)

    This PR was merged into the 3.2 branch.

    Discussion
    ----------

    [VarDumper] Use default color for ellipsed namespaces/paths

    | Q             | A
    | ------------- | ---
    | Branch?       | 3.2
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #20669
    | License       | MIT
    | Doc PR        | -

    Eases readability by helping the eye to focus more quickly on the class name and less on its namespace.
    And let' disable ellipses on the profiler panels, fixing #20669 meanwhile.

    ![capture du 2016-11-29 11-00-00](https://cloud.githubusercontent.com/assets/243674/20705475/5d512c9a-b623-11e6-881d-04ae58453824.png)

    Commits
    -------

    ebc23cf [VarDumper] Use default color for ellipsed namespaces/paths