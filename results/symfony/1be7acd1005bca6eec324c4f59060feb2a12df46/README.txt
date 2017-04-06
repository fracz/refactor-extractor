commit 1be7acd1005bca6eec324c4f59060feb2a12df46
Merge: 4a0adba 7b6161c
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 17 11:45:32 2014 +0200

    Merge branch '2.3' into 2.4

    * 2.3: (35 commits)
      [Form] Fix PHPDoc for builder setData methods The underlying data variable is typed as mixed whereas the methods paramers where typed as array.
      fixed CS
      [Intl] Improved bundle reader implementations
      [Console] guarded against invalid aliases
      switch before_script to before_install and script to install
      fixed typo
      [HttpFoundation] Request - URI - comment improvements
      [Security] Added more tests
      remove `service` parameter type from XSD
      [Intl] Added exception handler to command line scripts
      [Intl] Fixed a few bugs in TextBundleWriter
      [Intl] Updated icu.ini up to ICU 53
      [Intl] Removed non-working $fallback argument from ArrayAccessibleResourceBundle
      Use separated function to resolve command and related arguments
      [SwiftmailerBridge] Bump allowed versions of swiftmailer
      [FrameworkBundle] Remove invalid markup
      [Intl] Added "internal" tag to all classes under Symfony\Component\Intl\ResourceBundle
      Remove routes for removed WebProfiler actions
      [Security] Fix usage of unexistent method in DoctrineAclCache.
      backport more error information from 2.6 to 2.3
      ...

    Conflicts:
            .travis.yml
            src/Symfony/Component/DependencyInjection/Loader/YamlFileLoader.php
            src/Symfony/Component/DependencyInjection/Tests/Loader/XmlFileLoaderTest.php
            src/Symfony/Component/HttpKernel/Kernel.php
            src/Symfony/Component/Process/PhpExecutableFinder.php