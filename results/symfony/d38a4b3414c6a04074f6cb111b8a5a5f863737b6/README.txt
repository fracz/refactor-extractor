commit d38a4b3414c6a04074f6cb111b8a5a5f863737b6
Merge: 71c3a35 76d3c9e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jun 20 19:41:51 2014 +0200

    Merge branch '2.5'

    * 2.5:
      fixed previous merge
      Added missing `break` statement
      don't disable constructor calls to mockups of classes that extend internal PHP classes
      Small comment update according to PSR-2
      [Yaml] fix overwriting of keys after merged map
      [Yaml] fix priority of sequence merges according to spec
      [Console] Fixed notice in QuestionHelper
      [Console] Fixed notice in DialogHelper
      [Yaml] refactoring of merges for performance
      [Console] remove weird use statement
      [HttpFoundation] Fixed Request::getPort returns incorrect value under IPv6
      [Filesystem] Fix test suite on OSX
      [FrameworkBundle] Redirect server output to /dev/null in case no verbosity is needed
      Add framework-bundle

    Conflicts:
            src/Symfony/Component/Yaml/Parser.php