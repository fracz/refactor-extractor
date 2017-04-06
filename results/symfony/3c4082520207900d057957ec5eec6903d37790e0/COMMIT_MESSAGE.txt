commit 3c4082520207900d057957ec5eec6903d37790e0
Merge: 4af1556 4281501
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Wed Mar 23 14:23:25 2016 +0100

    Merge branch '2.8' into 3.0

    * 2.8: (22 commits)
      Fix backport
      [travis] Upgrade phpunit wrapper & hirak/prestissimo
      [Bridge\PhpUnit] Workaround old phpunit bug, no colors in weak mode, add tests
      [PropertyAccess] Fix isPropertyWritable not using the reflection cache
      [PropertyAccess] Backport fixes from 2.7
      [FrameworkBundle][2.8] Add tests for the Controller class
      [DependencyInjection] Update changelog
      Added WebProfiler toolbar ajax panel table layout css.
      [Validator] use correct term for a property in docblock (not "option")
      [Routing] small refactoring for scheme requirement
      [PropertyAccess] Remove most ref mismatches to improve perf
      [PropertyInfo] Support Doctrine custom mapping type in DoctrineExtractor
      [Validator] EmailValidator cannot extract hostname if email contains multiple @ symbols
      [NumberFormatter] Fix invalid numeric literal on PHP 7
      [Process] fix docblock syntax
      use the clock mock for progress indicator tests
      Use XML_ELEMENT_NODE in nodeType check
      [PropertyAccess] Reduce overhead of UnexpectedTypeException tracking
      [PropertyAccess] Throw an UnexpectedTypeException when the type do not match
      [FrameworkBundle] Add tests for the Controller class
      ...

    Conflicts:
            .travis.yml
            composer.json
            src/Symfony/Bridge/PhpUnit/DeprecationErrorHandler.php
            src/Symfony/Bundle/FrameworkBundle/composer.json
            src/Symfony/Component/Intl/NumberFormatter/NumberFormatter.php