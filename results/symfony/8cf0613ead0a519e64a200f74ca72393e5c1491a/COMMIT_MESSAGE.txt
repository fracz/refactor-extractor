commit 8cf0613ead0a519e64a200f74ca72393e5c1491a
Merge: 03f6c04 0191aa2
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Sat Apr 18 17:11:06 2015 +0200

    Merge branch '2.6' into 2.7

    * 2.6:
      [Routing][DependencyInjection] Support .yaml extension in YAML loaders
      [DX] improve file loader error for router/other resources in bundle
      [FrameworkBundle] Initialize translator with the default locale.
      [FrameworkBundle] Fix Routing\DelegatingLoader resiliency to fatal errors
      [2.6][Translation] remove duplicate code for loading catalogue.
      [HttpKernel] Cleanup ExceptionListener
      CS fixes
      [DependencyInjection] Show better error when the Yaml component is not installed
      [2.3] SCA for Components - reference mismatches
      [Debug] Scream as LogLevel::DEBUG (but for fatal errors / uncaught exceptions)
      [2.3] Static Code Analysis for Components
      [WebProfilerBundle] Fix resiliency to exceptions thrown by the url generator
      [Translation] LoggingTranslator simplifications
      [Translation][fixed test] refresh cache when resources are no longer fresh.
      [FrameworkBundle] Fixed server:start --router relative path issue #14124
      [FrameworkBundle] improve usage of Table helper
      [Validator] Added missing Simplified Chinese (zh_CN) translations
      [FrameworkBundle] Workaround php -S ignoring auto_prepend_file

    Conflicts:
            src/Symfony/Bundle/FrameworkBundle/Tests/Translation/TranslatorTest.php
            src/Symfony/Component/Console/Helper/Table.php
            src/Symfony/Component/Translation/LoggingTranslator.php