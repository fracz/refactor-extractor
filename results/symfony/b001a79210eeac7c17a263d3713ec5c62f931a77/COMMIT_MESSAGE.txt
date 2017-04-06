commit b001a79210eeac7c17a263d3713ec5c62f931a77
Merge: 12cf04f 1ea0f86
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 4 11:12:46 2015 +0100

    feature #13840 [WebProfilerBundle] Update ajax calls in toolbar to add the css error class (rubenrua)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [WebProfilerBundle] Update ajax calls in toolbar to add the css error class

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

    Minor change to improve the toolbar usability. The request method is red when an error response is received:

    ![toolbar ajax error](https://cloud.githubusercontent.com/assets/195745/6475331/2b390a26-c209-11e4-8dee-ef2e973b57c0.png)

    Commits
    -------

    1ea0f86 [WebProfilerBundle] Update ajax calls in toolbar to add the css error class