commit 96bfdacb3f4cb6f583c382c0000f9d881c27eb84
Merge: ff4d446 c688166
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat May 26 08:34:37 2012 +0200

    merged branch zerkalica/master (PR #4328)

    Commits
    -------

    c688166 [Form] fixing form type translation_domain inheritance fixed bug with parent property fix is_required field fixed subform translation_domain inheritance some translation_domain inheritance code refactoring added form type translation_domain inheritance tests changed methods place in form type test changed arguments in createNamed method call in FormTypeTest

    Discussion
    ----------

    FormType translation_domain inheritance

    Fixes: #3286, #3872, #3938

    I found bug in form type parameters inheritance. It's better to inherit some child form element options from parent: translation_domain, required.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-18T13:11:45Z

    This pull request [fails](http://travis-ci.org/symfony/symfony/builds/1366275) (merged afc32247 into 1e15f210).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-18T13:14:46Z

    This pull request [fails](http://travis-ci.org/symfony/symfony/builds/1366288) (merged 1794dc79 into 1e15f210).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-18T13:54:41Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1366673) (merged 2f5d0377 into 1e15f210).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-18T14:53:58Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1367306) (merged a4027f38 into 1e15f210).

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-20T14:55:14Z

    This PR has a few outstanding issues and is missing some tests (check read_only tests in FormTypeTest). Otherwise this looks good.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-22T04:48:47Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1396892) (merged 68534d7e into 1e15f210).

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-22T07:36:47Z

    Looks good. Could you please squash your commits into one commit (git rebase -i) and add the prefix "[Form]" to the message of that commit?

    ---------------------------------------------------------------------------

    by zerkalica at 2012-05-22T09:36:33Z

    Ok, but how to push rebased branch ? Use forced push (git push origin :master) or make new branch and new pull request ?

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-22T09:43:15Z

        git push -f origin master

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-22T09:53:13Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1398573) (merged 05a57095 into e4e3ce6c).

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-22T09:56:29Z

    Could you please add a test to FormTypeTest to verify that this actually works? The test already contains tests for the "read_only" functionality that can serve you as template.

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-25T06:26:26Z

    You need to rebase the branch on symfony master so that it becomes mergable:

        git rebase symfony/master

    Also, please squash the 8 commits into one.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-25T17:23:01Z

    This pull request [fails](http://travis-ci.org/symfony/symfony/builds/1435037) (merged 57ee25a1 into ff4d446c).