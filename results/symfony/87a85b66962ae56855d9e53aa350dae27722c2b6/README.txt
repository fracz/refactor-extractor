commit 87a85b66962ae56855d9e53aa350dae27722c2b6
Merge: e9df732 33e9d00
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jan 7 16:44:13 2013 +0100

    merged branch bschussek/issue6558 (PR #6578)

    This PR was merged into the 2.1 branch.

    Commits
    -------

    33e9d00 [Form] Deleted references in FormBuilder::getFormConfig() to improve performance

    Discussion
    ----------

    [Form] Deleted references in FormBuilder::getFormConfig() to improve performance

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: #6558
    Todo: -
    License of the code: MIT
    Documentation PR: -

    ---------------------------------------------------------------------------

    by vicb at 2013-01-07T11:09:24Z

    > Backwards compatibility break: no

    Really ?

    ---------------------------------------------------------------------------

    by vicb at 2013-01-07T12:27:37Z

    Adding a public method is a BC break.

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-07T12:42:14Z

    The method is inherited from the parent class, so the change should not affect BC.

    ---------------------------------------------------------------------------

    by vicb at 2013-01-07T13:27:21Z

    my bad.