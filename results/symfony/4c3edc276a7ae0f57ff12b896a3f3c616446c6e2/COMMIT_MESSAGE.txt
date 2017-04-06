commit 4c3edc276a7ae0f57ff12b896a3f3c616446c6e2
Merge: 99a66c9 521fcb1
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Dec 13 19:25:06 2012 +0100

    Merge branch '2.1'

    * 2.1:
      [Console] Add support for parsing terminal width/height on localized windows, fixes #5742
      [Form] Fixed treatment of countables and traversables in Form::isEmpty()
      refactor ControllerNameParser
      [Form] Fixed FileType not to throw an exception when bound empty
      - Test undefined index #
      Maintain array structure
      Check if key # is defined in $value
      Update src/Symfony/Component/Validator/Resources/translations/validators.pl.xlf