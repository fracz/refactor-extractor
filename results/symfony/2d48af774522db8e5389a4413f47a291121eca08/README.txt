commit 2d48af774522db8e5389a4413f47a291121eca08
Merge: 88d586b 54bbade
Author: Tobias Schultze <webmaster@tubo-world.de>
Date:   Tue Dec 15 20:21:59 2015 +0100

    bug #17006 [Form] Fix casting regression in DoctrineChoiceLoader (bendavies)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [Form] Fix casting regression in DoctrineChoiceLoader

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | N/A
    | License       | MIT
    | Doc PR        | N/A

    In symfony 2.7, the [DoctrineChoiceLoader](https://github.com/symfony/symfony/blob/9543b36a34221aaca9f2f3cfe34112e8468a591a/src/Symfony/Bridge/Doctrine/Form/ChoiceList/DoctrineChoiceLoader.php) and [IdReader](https://github.com/symfony/symfony/blob/9543b36a34221aaca9f2f3cfe34112e8468a591a/src/Symfony/Bridge/Doctrine/Form/ChoiceList/IdReader.php) were introduce to replace the deprecated [EntityChoiceList](https://github.com/symfony/symfony/blob/9543b36a34221aaca9f2f3cfe34112e8468a591a/src/Symfony/Bridge/Doctrine/Form/ChoiceList/EntityChoiceList.php)

    There appears to have been a change in behaviour in the refactor, as the old `EntityChoiceList` [casts ID to strings](https://github.com/symfony/symfony/blob/9543b36a34221aaca9f2f3cfe34112e8468a591a/src/Symfony/Bridge/Doctrine/Form/ChoiceList/EntityChoiceList.php#L248), whereas the new `DoctrineChoiceLoader` [does not](https://github.com/symfony/symfony/blob/2.7/src/Symfony/Bridge/Doctrine/Form/ChoiceList/DoctrineChoiceLoader.php#L159). The casting behavior was [maintained elsewhere](https://github.com/symfony/symfony/blob/2.7/src/Symfony/Bridge/Doctrine/Form/ChoiceList/DoctrineChoiceLoader.php#L122), however.

    Since the new `DoctrineChoiceLoader` deprecated `EntityChoiceList`, i'm calling this a regression.

    Commits
    -------

    54bbade [Form] cast IDs to match deprecated behaviour of EntityChoiceList