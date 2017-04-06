commit 9cdc9712b03101320434371d9aea83789200616e
Merge: ee9c986 fc7c7f6
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 21 18:41:37 2012 +0100

    merged branch vicb/form/guess/length (PR #3645)

    Commits
    -------

    fc7c7f6 [Form] Fix min/max length guessing for numeric types (fix #3091)

    Discussion
    ----------

    [Form] Fix min/max length guessing for numeric types (fix #3091)

    Before this PR, the length was guessed from `strlen(min/max)`.

    This is obviously false for float: `strlen("1.123") > strlen ("5")` then this guess is now low confidence only and is masked by a `null` medium confidence guess for floats (implemented in both doctrine ORM & validator).

    This PR also includes some code reorg in order to improve readability.

    I'll update Propel & Mongo if needed once this is merged.

    _note: `5.000` did neither work because of `5e3`_

    ---------------------------------------------------------------------------

    by Koc at 2012-03-19T23:42:01Z

    Will `strlen` works correctly with multibyte strings?

    ---------------------------------------------------------------------------

    by vicb at 2012-03-19T23:58:33Z

    could numeric types be multibyte strings ?

    ---------------------------------------------------------------------------

    by Koc at 2012-03-20T00:07:24Z

    I thought it somehow concerns `Symfony\Component\Validator\Constraints\MaxLengthValidator` too.

    ---------------------------------------------------------------------------

    by vicb at 2012-03-20T00:20:33Z

    This PR is about numeric types only and the MaxLengthValidator is [multibyte safe:](https://github.com/symfony/symfony/blob/master/src/Symfony/Component/Validator/Constraints/MaxLengthValidator.php#L45)