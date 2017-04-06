commit 0191aa24bbe16d902d373b68c7f7bc6755c3c602
Merge: b993027 1014719
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Sat Apr 18 17:02:51 2015 +0200

    Merge branch '2.3' into 2.6

    * 2.3:
      [Routing][DependencyInjection] Support .yaml extension in YAML loaders
      [DX] improve file loader error for router/other resources in bundle
      [FrameworkBundle] Fix Routing\DelegatingLoader resiliency to fatal errors
      [HttpKernel] Cleanup ExceptionListener
      CS fixes
      [DependencyInjection] Show better error when the Yaml component is not installed
      [2.3] SCA for Components - reference mismatches
      [2.3] Static Code Analysis for Components
      [Translation][fixed test] refresh cache when resources are no longer fresh.
      [Validator] Added missing Simplified Chinese (zh_CN) translations
      [FrameworkBundle] Workaround php -S ignoring auto_prepend_file

    Conflicts:
            src/Symfony/Bundle/FrameworkBundle/Tests/Translation/TranslatorTest.php
            src/Symfony/Component/Config/Exception/FileLoaderLoadException.php
            src/Symfony/Component/Console/Descriptor/TextDescriptor.php
            src/Symfony/Component/Console/Helper/TableHelper.php
            src/Symfony/Component/Console/Tests/Formatter/OutputFormatterTest.php
            src/Symfony/Component/DependencyInjection/Dumper/PhpDumper.php
            src/Symfony/Component/DependencyInjection/Dumper/YamlDumper.php
            src/Symfony/Component/HttpKernel/Debug/TraceableEventDispatcher.php
            src/Symfony/Component/HttpKernel/Tests/Debug/TraceableEventDispatcherTest.php
            src/Symfony/Component/PropertyAccess/PropertyAccessor.php
            src/Symfony/Component/Yaml/Tests/InlineTest.php