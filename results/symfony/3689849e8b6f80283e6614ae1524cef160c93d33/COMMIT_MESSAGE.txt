commit 3689849e8b6f80283e6614ae1524cef160c93d33
Merge: 702e652 d05ab6b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Sep 7 18:29:51 2013 +0200

    Merge branch '2.2' into 2.3 (closes #8955)

    * 2.2:
      [HttpFoundation] removed extra parenthesis
      [Process][2.2] Fix Process component on windows
      [HttpFoundation] improve perf of previous merge (refs #8882)
      Request->getPort() should prefer HTTP_HOST over SERVER_PORT
      Fixing broken http auth digest in some circumstances (php-fpm + apache).
      fixed typo

    Conflicts:
            src/Symfony/Component/Process/Process.php