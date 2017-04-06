commit 52511770027c84a4ff38851c08c8d61f88df6db2
Merge: ffef177 8321593
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Feb 4 07:59:57 2012 +0100

    merged branch helmer/readonly_fix (PR #3258)

    Commits
    -------

    8321593 [Form] DRYed ChoiceType
    0753cee [Form] Fixed read_only attribute for expanded fields

    Discussion
    ----------

    [Form] Fixed read_only attribute for expanded fields

    Expanded choice widgets lose the knowledge of read_only attribute value.

    Fixes bug introduced by #3193

    ---------------------------------------------------------------------------

    by helmer at 2012-02-02T16:24:50Z

    Please hold before merging, @bschussek had some thoughts about my changes in ``ChoiceType``, waiting for feedback.

    ---------------------------------------------------------------------------

    by bschussek at 2012-02-02T16:33:12Z

    I'm fine with the refactoring then, but please split it into two commits at least. The changes in ChoiceType have nothing in common with the actual issue here.

    ---------------------------------------------------------------------------

    by helmer at 2012-02-02T19:40:39Z

    Tests added.

    ---------------------------------------------------------------------------

    by bschussek at 2012-02-03T10:14:32Z

    Great, thanks.

    @fabpot :+1: