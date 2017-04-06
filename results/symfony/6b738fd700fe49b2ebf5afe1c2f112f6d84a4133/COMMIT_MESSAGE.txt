commit 6b738fd700fe49b2ebf5afe1c2f112f6d84a4133
Merge: a541193 f5a0ac2
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Fri Apr 24 09:09:27 2015 +0200

    Merge branch '2.8'

    * 2.8: (61 commits)
      [Debug] Fix ClassNotFoundFatalErrorHandler candidates lookups
      [2.6][Translator] Extend, refactor and simplify Translator tests.
      [VarDumper] Allow preserving a subset of cut arrays
      [Console] Bind the closure (code) to the Command if possible
      [VarDumper] Added support for SplFileObject
      [VarDumper] Added support for SplFileInfo
      Update DebugClassLoader.php
      inject asset packages in assets helper service
      [travis] Do not exclude legacy tests on 2.7
      [HttpFoundation] remove getExtension method
      [2.6][Translation] fix legacy tests.
      [Form] Removed remaining deprecation notices in the test suite
      [Form] Moved deprecation notice triggers to file level
      [Debug] Map PHP errors to LogLevel::CRITICAL
      [FrameworkBundle][Server Command] add address port number option.
      [Routing][DependencyInjection] Support .yaml extension in YAML loaders
      [DX] improve file loader error for router/other resources in bundle
      [FrameworkBundle] Initialize translator with the default locale.
      [FrameworkBundle] Fix Routing\DelegatingLoader resiliency to fatal errors
      [2.7][Translation] remove duplicate code for loading catalogue.
      ...

    Conflicts:
            composer.json
            src/Symfony/Bridge/Swiftmailer/composer.json
            src/Symfony/Component/Console/Helper/DialogHelper.php
            src/Symfony/Component/Debug/ErrorHandler.php
            src/Symfony/Component/Debug/Tests/FatalErrorHandler/ClassNotFoundFatalErrorHandlerTest.php
            src/Symfony/Component/Form/Extension/HttpFoundation/EventListener/BindRequestListener.php
            src/Symfony/Component/Locale/composer.json