commit 0d2757014e8911dfeab0e2f35548b71129752990
Merge: cd08db8 9d730be
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Jun 28 16:11:59 2012 +0200

    merged branch jeanmonod/config-unittest-on-exprbuilder (PR #4570)

    Commits
    -------

    9d730be Method rename and phpdoc fixes
    e01a95e Add a set of unit tests for the ExprBuilder class

    Discussion
    ----------

    Add a set of unit tests for the Config/ExprBuilder class

    ---------------------------------------------------------------------------

    by travisbot at 2012-06-13T14:55:31Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1611400) (merged e01a95e1 into c07e9163).

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-06-13T22:04:52Z

    Hi there,

    I write all these tests because I'll come latter with an other PR that extend the ExprBuilder functionality. But I'm not sure I use the best way for testing this class. It's working, but some refactoring suggestions will be welcome...
    @stof and @schmittjoh what do you think about that?