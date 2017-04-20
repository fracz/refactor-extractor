commit fa6f1df3b2d60380ac2bf6e5a4c0b39688ba8210
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Wed Jun 8 15:50:39 2011 -0500

    Rename from ScannerFoo to FooScanner

    - Renamed to be consistent with other similar usage elsewhere
      - Reflection API is exempted from this renaming, as it mimics an internal API
        (which uses pseudo-namespaces)
      - CodeGenerator may require similar refactoring