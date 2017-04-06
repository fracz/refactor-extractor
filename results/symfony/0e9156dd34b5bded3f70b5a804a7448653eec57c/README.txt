commit 0e9156dd34b5bded3f70b5a804a7448653eec57c
Merge: 867e31c 492c990
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Aug 29 13:43:12 2012 +0200

    merged branch Tobion/formrefactor (PR #5338)

    Commits
    -------

    492c990 [Form] optimized PropertyPathMapper to invoke the expensive property path less often
    47a8bbd [Form] optimized the binding of child forms and calculation of extra data
    8d45539 [Form] refactor Form::bind to save 7 assignments

    Discussion
    ----------

    [Form] refactor Form::bind to save 7 assignments and a complete loop

    ---------------------------------------------------------------------------

    by stof at 2012-08-24T23:45:18Z

    the new code is not equivalent. See travis for the proof.

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-25T01:50:41Z

    @stof fixed, I had to reduce the refactoring a little

    ---------------------------------------------------------------------------

    by bschussek at 2012-08-29T11:05:52Z

    :+1: