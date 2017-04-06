commit 4677e92ffeac80be14055c049dd94bdcb4f315ef
Merge: 9046c48 687703a
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 17 11:54:14 2014 +0200

    Merge branch '2.5'

    * 2.5: (43 commits)
      [Form] Fix PHPDoc for builder setData methods The underlying data variable is typed as mixed whereas the methods paramers where typed as array.
      fixed CS
      [Intl] Improved bundle reader implementations
      [Console] guarded against invalid aliases
      switch before_script to before_install and script to install
      fixed typo
      [HttpFoundation] Request - URI - comment improvements
      [Validator] The ratio of the ImageValidator is rounded to two decimals now
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
      ...

    Conflicts:
            src/Symfony/Bundle/FrameworkBundle/Command/ServerRunCommand.php
            src/Symfony/Component/HttpKernel/HttpCache/Esi.php
            src/Symfony/Component/HttpKernel/Kernel.php
            src/Symfony/Component/Translation/Tests/Dumper/XliffFileDumperTest.php
            src/Symfony/Component/Yaml/Parser.php
            src/Symfony/Component/Yaml/Tests/InlineTest.php