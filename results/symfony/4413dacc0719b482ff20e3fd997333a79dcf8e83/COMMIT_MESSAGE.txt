commit 4413dacc0719b482ff20e3fd997333a79dcf8e83
Merge: 9b5f56c 20bf24e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jul 28 15:13:16 2014 +0200

    Merge branch '2.3' into 2.4

    * 2.3:
      Update validators.eu.xlf
      fixed CS
      remove unused imports
      Unify null comparisons
      [EventDispatcher] don't count empty listeners
      [Process] Fix unit tests in sigchild environment
      [Process] fix signal handling in wait()
      [BrowserKit] refactor code and fix unquoted regex
      Fixed server HTTP_HOST port uri conversion
      Bring code into standard
      [Process] Add test to verify fix for issue #11421
      [Process] Fixes issue #11421
      [DependencyInjection] Pass a Scope instance instead of a scope name.

    Conflicts:
            src/Symfony/Component/EventDispatcher/Tests/EventDispatcherTest.php