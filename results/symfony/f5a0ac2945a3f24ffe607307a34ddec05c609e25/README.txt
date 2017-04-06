commit f5a0ac2945a3f24ffe607307a34ddec05c609e25
Merge: a4e3e07 1b34f2c
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Fri Apr 24 09:02:53 2015 +0200

    Merge branch '2.7' into 2.8

    * 2.7: (40 commits)
      [Debug] Fix ClassNotFoundFatalErrorHandler candidates lookups
      [2.6][Translator] Extend, refactor and simplify Translator tests.
      Update DebugClassLoader.php
      inject asset packages in assets helper service
      [travis] Do not exclude legacy tests on 2.7
      [HttpFoundation] remove getExtension method
      [2.6][Translation] fix legacy tests.
      [Form] Removed remaining deprecation notices in the test suite
      [Form] Moved deprecation notice triggers to file level
      [Debug] Map PHP errors to LogLevel::CRITICAL
      [Routing][DependencyInjection] Support .yaml extension in YAML loaders
      [DX] improve file loader error for router/other resources in bundle
      [FrameworkBundle] Initialize translator with the default locale.
      [FrameworkBundle] Fix Routing\DelegatingLoader resiliency to fatal errors
      [2.7][Translation] remove duplicate code for loading catalogue.
      [2.6][Translation] remove duplicate code for loading catalogue.
      [HttpKernel] Cleanup ExceptionListener
      CS fixes
      [DependencyInjection] Show better error when the Yaml component is not installed
      [2.3] SCA for Components - reference mismatches
      ...