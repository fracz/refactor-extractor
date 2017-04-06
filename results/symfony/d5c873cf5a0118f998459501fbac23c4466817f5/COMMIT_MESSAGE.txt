commit d5c873cf5a0118f998459501fbac23c4466817f5
Merge: a8e2c74 2c4b5e5
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Mar 30 17:55:07 2015 +0200

    Merge branch '2.6' into 2.7

    * 2.6:
      [Validator] Add missing pt_BR translations
      Add parsing of hexadecimal strings for PHP 7
      [Configuration] improve description for ignoreExtraKeys on ArrayNodeDefinition
      [Validator] Added missing Hungarian translation
      [Validator] Fixed grammar in Hungarian translation
      CS: Unary operators should be placed adjacent to their operands
      CS: Binary operators should be arounded by at least one space
      remove useless tests that fail in php 7
      [Translator] fix test for php 7 compatibility
      Update phpdoc of ProcessBuilder#setPrefix()

    Conflicts:
            src/Symfony/Bridge/Propel1/Logger/PropelLogger.php
            src/Symfony/Component/Validator/Resources/translations/validators.hu.xlf