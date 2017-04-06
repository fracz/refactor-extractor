commit 9687d1661b4ea48a28d8de0c85b9257128d059c1
Merge: 2123006 e0ff619
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat May 14 10:59:02 2011 +0200

    Merge remote branch 'bschussek/form_validator'

    * bschussek/form_validator:
      [Form] Renamed the value "text" of the "widget" option of the "date" type to "single-text"
      [Form] Implemented getAllowedOptionValues() for core types
      [Form] Removed unused option
      [Form] Added FormTypeInterface::getAllowedOptionValues() to better validate passed options
      [Form] Improved test coverage of FormFactory and improved error handling
      [Form] Added getType() to FormFactoryInterface
      [Validator] Refactoring DateTimeValidator and DateValidator
      [Validator] Date: check if the value is a DateTime instance