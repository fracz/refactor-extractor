commit 8e8488092fe808bdb956e7506d84f401d874741d
Merge: ef399b1 2e30a43
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Sep 23 08:15:19 2014 +0200

    feature #11815 Added some methods to improve the handling of auto_mapping feature (goetas)

    This PR was squashed before being merged into the 2.6-dev branch (closes #11815).

    Discussion
    ----------

    Added some methods to improve the handling of auto_mapping feature

    | Q             | A
    | ------------- | ---
    | Bug fix?      |no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?  | yes
    | Fixed tickets | https://github.com/doctrine/DoctrineBundle/issues/60
    | License       | MIT

    This PR is a part that is required by:
    * https://github.com/doctrine/DoctrineMongoDBBundle/pull/267
    * https://github.com/doctrine/DoctrineBundle/pull/321

    The proposed PRs are an alternative to https://github.com/symfony/symfony/pull/11650 and https://github.com/doctrine/DoctrineBundle/pull/322

    Commits
    -------

    2e30a43 Added some methods to improve the handling of auto_mapping feature