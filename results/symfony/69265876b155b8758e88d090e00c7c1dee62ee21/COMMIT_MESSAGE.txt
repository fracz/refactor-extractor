commit 69265876b155b8758e88d090e00c7c1dee62ee21
Merge: 091a96c 3689849
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Sep 7 18:33:13 2013 +0200

    Merge branch '2.3' (closes #8956)

    * 2.3:
      [HttpFoundation] removed extra parenthesis
      [Process][2.2] Fix Process component on windows
      [HttpFoundation] improve perf of previous merge (refs #8882)
      Request->getPort() should prefer HTTP_HOST over SERVER_PORT
      Fixing broken http auth digest in some circumstances (php-fpm + apache).
      fixed typo

    Conflicts:
            src/Symfony/Component/Process/Process.php