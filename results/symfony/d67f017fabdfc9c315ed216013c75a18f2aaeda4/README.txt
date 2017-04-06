commit d67f017fabdfc9c315ed216013c75a18f2aaeda4
Merge: 01cebda d5c873cf
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Mar 30 17:59:03 2015 +0200

    Merge branch '2.7'

    * 2.7:
      [RFC][Console] Added console style guide helpers (v2)
      [Validator] Add missing pt_BR translations
      [Translation][Profiler] Added a Translation profiler.
      Add parsing of hexadecimal strings for PHP 7
      [VarDumper] Add filters to casters
      Trim final stop from deprecation message
      [Configuration] improve description for ignoreExtraKeys on ArrayNodeDefinition
      [Validator] Added missing Hungarian translation
      [Validator] Fixed grammar in Hungarian translation
      CS: Unary operators should be placed adjacent to their operands
      CS: Binary operators should be arounded by at least one space
      remove useless tests that fail in php 7
      [Translator] fix test for php 7 compatibility
      [VarDumper] Add VarDumperTestCase and related trait
      Update phpdoc of ProcessBuilder#setPrefix()

    Conflicts:
            src/Symfony/Bridge/ProxyManager/Tests/LazyProxy/Fixtures/php/lazy_service.php
            src/Symfony/Component/Routing/Matcher/ApacheUrlMatcher.php
            src/Symfony/Component/Routing/Matcher/Dumper/ApacheMatcherDumper.php
            src/Symfony/Component/VarDumper/Cloner/AbstractCloner.php