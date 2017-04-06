commit 7e175ef8f3707d12eaf2025ebfb5f0c3e32129a1
Merge: e8b53e9 4413dac
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jul 28 15:20:46 2014 +0200

    Merge branch '2.4' into 2.5

    * 2.4:
      Update validators.eu.xlf
      fixed CS
      remove unused imports
      [Routing] simplify the XML schema file
      Unify null comparisons
      [EventDispatcher] don't count empty listeners
      [Process] Fix unit tests in sigchild environment
      [Process] fix signal handling in wait()
      [BrowserKit] refactor code and fix unquoted regex
      Fixed server HTTP_HOST port uri conversion
      [MonologBridge] fixed Console handler priorities
      Bring code into standard
      [Process] Add test to verify fix for issue #11421
      [Process] Fixes issue #11421
      [DependencyInjection] Pass a Scope instance instead of a scope name.

    Conflicts:
            src/Symfony/Bundle/FrameworkBundle/Test/WebTestCase.php
            src/Symfony/Component/DependencyInjection/Tests/Dumper/GraphvizDumperTest.php
            src/Symfony/Component/PropertyAccess/Tests/PropertyAccessorCollectionTest.php