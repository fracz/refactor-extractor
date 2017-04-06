commit 42815018ed8f04a46726622a5b2cd47aa9c3b2ef
Merge: dd7e05d 2b3f426
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Wed Mar 23 14:11:46 2016 +0100

    Merge branch '2.7' into 2.8

    * 2.7:
      Fix backport
      [travis] Upgrade phpunit wrapper & hirak/prestissimo
      [Bridge\PhpUnit] Workaround old phpunit bug, no colors in weak mode, add tests
      [PropertyAccess] Fix isPropertyWritable not using the reflection cache
      [PropertyAccess] Backport fixes from 2.7
      [Validator] use correct term for a property in docblock (not "option")
      [Routing] small refactoring for scheme requirement
      [PropertyAccess] Remove most ref mismatches to improve perf
      [Validator] EmailValidator cannot extract hostname if email contains multiple @ symbols
      [NumberFormatter] Fix invalid numeric literal on PHP 7
      [Process] fix docblock syntax
      Use XML_ELEMENT_NODE in nodeType check
      [PropertyAccess] Reduce overhead of UnexpectedTypeException tracking
      [PropertyAccess] Throw an UnexpectedTypeException when the type do not match
      [FrameworkBundle] Add tests for the Controller class
      [FrameworkBundle] Add tests for the Controller class
      [Process] getIncrementalOutput should work without calling getOutput

    Conflicts:
            src/Symfony/Bridge/PhpUnit/DeprecationErrorHandler.php
            src/Symfony/Bridge/PhpUnit/TextUI/Command.php
            src/Symfony/Bundle/FrameworkBundle/Tests/Controller/ControllerTest.php