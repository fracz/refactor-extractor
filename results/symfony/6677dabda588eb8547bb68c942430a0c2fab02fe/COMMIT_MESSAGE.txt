commit 6677dabda588eb8547bb68c942430a0c2fab02fe
Merge: adb7291 d19f1d7
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Sep 2 09:43:19 2011 +0200

    merged branch jmikola/entity-validator (PR #2076)

    Commits
    -------

    d19f1d7 [Doctrine] Fix UniqueEntityValidator reporting a false positive by ignoring multiple query results

    Discussion
    ----------

    [Doctrine] Fix UniqueEntityValidator reporting a false positive by ignoring multiple query results

    An entity should only be considered unique if its search criteria returns no matches or a single, identical entity. Multiple results indicates that conflicting entities exist.

    Note: the DoctrineMongoDBBundle's unique validator checks identifier values if the object strict-equality check is false. This may be a worthwhile improvement, as it would prevent reporting a validation error for an enttiy which is going to overwrite its conflicting counter-part in the database.

    ---------------------------------------------------------------------------

    by jmikola at 2011/09/01 14:23:27 -0700

    This is the Doctrine bridge equivalent for my fix to DoctrineMongoDBBundle: https://github.com/symfony/DoctrineMongoDBBundle/pull/42

    ---------------------------------------------------------------------------

    by fabpot at 2011/09/02 00:13:52 -0700

    As this is a bug fix, can you base your PR on the symfony/2.0 branch? Thanks.