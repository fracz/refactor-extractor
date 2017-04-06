commit 86c0a17721fab3cfc0caa122e5c0f7c80ece81f1
Merge: 46e8229 d01a106
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Tue Mar 22 09:55:46 2016 +0100

    Merge branch '2.3' into 2.7

    * 2.3:
      [Validator] use correct term for a property in docblock (not "option")
      [PropertyAccess] Remove most ref mismatches to improve perf
      [Validator] EmailValidator cannot extract hostname if email contains multiple @ symbols
      [NumberFormatter] Fix invalid numeric literal on PHP 7
      Use XML_ELEMENT_NODE in nodeType check
      [PropertyAccess] Reduce overhead of UnexpectedTypeException tracking
      [PropertyAccess] Throw an UnexpectedTypeException when the type do not match
      [FrameworkBundle] Add tests for the Controller class

    Conflicts:
            src/Symfony/Bundle/FrameworkBundle/Tests/Controller/ControllerTest.php
            src/Symfony/Component/Intl/NumberFormatter/NumberFormatter.php
            src/Symfony/Component/PropertyAccess/PropertyAccessor.php
            src/Symfony/Component/PropertyAccess/PropertyAccessorInterface.php
            src/Symfony/Component/PropertyAccess/PropertyPath.php
            src/Symfony/Component/PropertyAccess/Tests/PropertyAccessorTest.php
            src/Symfony/Component/Validator/Constraints/EmailValidator.php