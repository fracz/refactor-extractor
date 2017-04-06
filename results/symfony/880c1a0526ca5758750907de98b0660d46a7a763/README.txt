commit 880c1a0526ca5758750907de98b0660d46a7a763
Merge: ade5de0 4ce9ac3
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Feb 11 11:54:19 2013 +0100

    merged branch jakzal/2.0-event (PR #7038)

    This PR was merged into the 2.0 branch.

    Commits
    -------

    4ce9ac3 [EventDispatcher] Added assertion.

    Discussion
    ----------

    [EventDispatcher] Added assertion

    re #7023, I think it actually makes sense to add an assertion here. It reveals the intent of the test (listener is not removed).

    ---------------------------------------------------------------------------

    by vicb at 2013-02-10T11:32:06Z

    I don't think the assertion would fail with the former code, would it ?

    ---------------------------------------------------------------------------

    by vicb at 2013-02-10T11:34:30Z

    I mean an error would be generated even before the assertion.

    ---------------------------------------------------------------------------

    by jakzal at 2013-02-10T11:37:05Z

    Yes, it would fail before getting to the assertion with: *Object of class Closure could not be converted to int*.

    However, this is something good to test for (and document - test is a documentation). We're not checking if type is taken into account in other tests. This test might still fail if code inside removeListener() changed.

    ---------------------------------------------------------------------------

    by vicb at 2013-02-10T11:42:29Z

    I don't really understand your point and think it is a bit useless here but I am not against your change - I don't argue that test is doc though.

    ---------------------------------------------------------------------------

    by jakzal at 2013-02-10T15:38:09Z

    Assertion is indeed useless for the bug you discovered and fixed. I think it's still worth to have it there for  other reason:
    * test readability and completeness - with an assertion it's more clear that we don't expect the listener to be removed with the `removeListener()` call if passed argument doesn't match the one added before

    If you still don't see my point just close this PR :)

    ---------------------------------------------------------------------------

    by vicb at 2013-02-10T17:34:35Z

    What I mean is that you are unit testing php and it is not a job for sf. So it is not strictly required but as it doesn't hurt, let's merge your change.

    Jakub Zalas <notifications@github.com> wrote:

    >Assertion is indeed useless for the bug you discovered and fixed. I
    >think it's still worth to have it there for  other reason:
    >* test readability and completeness - with an assertion it's more clear
    >that we don't expect the listener to be removed with the
    >`removeListener()` call if passed argument doesn't match the one added
    >before
    >
    >If you still don't see my point just close this PR :)
    >
    >
    >---
    >Reply to this email directly or view it on GitHub:
    >https://github.com/symfony/symfony/pull/7038#issuecomment-13351469