commit fd1be6f09b66ee3fd1dfece2ff132e8436c90447
Merge: de1714b 4b68eb1
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Thu Sep 10 10:18:49 2015 +0200

    Merge branch '2.8'

    * 2.8:
      [HtppKernel] deprecated Profiler::import/export
      [Validator] Added Swedish translations
      [WebProfilerBundle ] Removes the ajax animation in | sf-toolbar-block-ajax
      improve exceptions when parsing malformed files
      Dispatch console.terminate *after* console.exception
      [HttpKernel] Move required RequestStack args as first arguments
      [WebProfilerBundle] deprecated import/export commands
      [Debug] Add BufferingLogger for errors that happen before a proper logger is configured

    Conflicts:
            UPGRADE-2.8.md
            src/Symfony/Bridge/Twig/composer.json
            src/Symfony/Bundle/FrameworkBundle/composer.json
            src/Symfony/Component/Debug/CHANGELOG.md
            src/Symfony/Component/HttpKernel/CHANGELOG.md