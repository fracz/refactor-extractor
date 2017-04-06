commit 81bf9108a0cbc12bc8efc7898ea8850e0e6395b4
Merge: c0ddd3d 3910940
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 8 07:37:48 2015 +0200

    feature #13220 [Console] Made output docopt compatible (WouterJ)

    This PR was squashed before being merged into the 2.7 branch (closes #13220).

    Discussion
    ----------

    [Console] Made output docopt compatible

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #6329
    | License       | MIT
    | Doc PR        | https://github.com/symfony/symfony-docs/issues/5016

    This was harder than I thought. To sum up:

     * The output now follows the [docopt](http://docopt.org/) specification
     * There is a new `addUsage` method to add more usage patterns
     * The handling of spaces in the descriptors is refactored to make it easier to understand and to make it render better (using sprintf's features only made it worse imo)

    Todo
    ---

     * [x] Add test for `addUsage` and friends
     * [x] Add test for multiline descriptions of arguments
     * <s>Convert long descriptions to multiline automatically</s>
     * [ ] Submit a doc PR for `addUsage`

    Question
    ---

    The docopt specification suggests we should add these usage patterns:

        %command.name% -h | --help
        %command.name% --version

    I didn't do that yet, as I think it'll only makes the output more verbose and it's already pretty obvious.

    I've taken some decisions which I don't think everybody agrees with. I'm willing to change it, so feel free to comment :)

    /cc @Seldaek

    Commits
    -------

    3910940 [Console] Made output docopt compatible