commit d53874b7478b27e337fb21c9e6ee17ef7987b0ea
Merge: 003507d 3c40825
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Wed Mar 23 14:33:04 2016 +0100

    Merge branch '3.0'

    * 3.0: (22 commits)
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
            src/Symfony/Bridge/PhpUnit/SymfonyTestsListener.php
            src/Symfony/Bundle/FrameworkBundle/Tests/Controller/ControllerTest.php
            src/Symfony/Bundle/FrameworkBundle/composer.json
            src/Symfony/Component/PropertyAccess/PropertyAccessor.php
            src/Symfony/Component/PropertyAccess/PropertyAccessorInterface.php
            src/Symfony/Component/PropertyAccess/Tests/PropertyAccessorTest.php