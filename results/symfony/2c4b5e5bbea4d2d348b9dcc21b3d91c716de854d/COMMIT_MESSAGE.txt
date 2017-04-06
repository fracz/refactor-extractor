commit 2c4b5e5bbea4d2d348b9dcc21b3d91c716de854d
Merge: 89c82e5 9215c22
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Mar 30 17:54:10 2015 +0200

    Merge branch '2.3' into 2.6

    * 2.3:
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
            src/Symfony/Component/HttpFoundation/Session/Attribute/NamespacedAttributeBag.php
            src/Symfony/Component/PropertyAccess/PropertyAccessor.php
            src/Symfony/Component/Validator/Resources/translations/validators.pt_BR.xlf
            src/Symfony/Component/Yaml/Parser.php