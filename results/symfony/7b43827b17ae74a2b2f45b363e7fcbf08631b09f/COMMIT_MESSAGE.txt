commit 7b43827b17ae74a2b2f45b363e7fcbf08631b09f
Merge: 7c5321c d009b2b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Oct 24 07:49:22 2014 +0200

    Merge branch '2.3' into 2.5

    * 2.3:
      enforce memcached version to be 2.1.0
      [FrameworkBundle] improve server:run feedback
      [Form] no need to add the url listener when it does not do anything
      [Form] Fix #11694 - Enforce options value type check in some form types
      Lithuanian security translations
      [Router] Cleanup
      [FrameworkBundle] Fixed ide links
      Add missing argument
      [TwigBundle] do not pass a template reference to twig
      [TwigBundle] show correct fallback exception template in debug mode
      [TwigBundle] remove unused email placeholder from error page
      use meta charset in layouts without legacy http-equiv

    Conflicts:
            src/Symfony/Bundle/TwigBundle/Loader/FilesystemLoader.php
            src/Symfony/Bundle/TwigBundle/Resources/views/layout.html.twig