commit d4c011d56d6b10b6b893898f4531be0437ab326d
Merge: bdb5275 98b68c2
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Oct 27 18:40:54 2012 +0200

    merged branch dirkaholic/master (PR #5638)

    This PR was squashed before being merged into the master branch (closes #5638).

    Commits
    -------

    98b68c2 [2.2][Console] Add possibility to add new input options to console application

    Discussion
    ----------

    [2.2][Console] Add possibility to add new input options to console application

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -
    License of the code: MIT
    Documentation PR: -

    At the moment it is not possible to overwrite the input arguments of a console application to not have the default ones. Adding is possible with:

    $cli->getDefinition()->addOption(new InputOption('--custom', '-c', InputOption::VALUE_NONE, 'Use custom option.'));

    Also added some simple tests for adding a custom HelperSet.

    ---------------------------------------------------------------------------

    by dirkaholic at 2012-10-04T06:29:57Z

    OK, is a bit inconsistent with what it's done with the helper set then, where you can use both ways. New PR for the tests is the referenced one.

    ---------------------------------------------------------------------------

    by stof at 2012-10-04T18:57:42Z

    @dirkaholic Can you rebase your branch (it conflicts with master) and squash your commit together ?

    http://symfony.com/doc/current/contributing/code/patches.html#rework-your-patch may help you if you don't know how to do it

    ---------------------------------------------------------------------------

    by dirkaholic at 2012-10-04T19:53:09Z

    Done.

    ---------------------------------------------------------------------------

    by stof at 2012-10-04T21:40:53Z

    @dirkaholic the rebase worked fine but you have not squashed the commits together.

    ---------------------------------------------------------------------------

    by dirkaholic at 2012-10-05T05:35:30Z

    What do you mean ? Only the setDefinition function plus test is left here. The rest was already merged with https://github.com/symfony/symfony/issues/5668

    ---------------------------------------------------------------------------

    by stof at 2012-10-05T10:48:53Z

    @dirkaholic Squashing is about making the PR use only 1 commit instead of 2 (the second one changing only some whitespaces, which is not what its message says). But @fabpot told me that he improved his merging tool and so he can squash it when merging so it is OK.