commit 36bcfcc5ee46aa699ce7dfdbc470ec673bd17b96
Merge: b7c8442 74cca63
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed May 4 22:13:33 2011 +0200

    Merge remote branch 'bschussek/form'

    * bschussek/form:
      [Form] CSRF fields are not included in the children of a FormView anymore if the view is not the root
      [Form] FormView::offsetUnset() is now supported. It was possible anyway using getChildren() and setChildren().
      [Form] Split the option "modifiable" of the "collection" type into "allow_add" and "allow_delete"
      [Form] Added test for last commit by kriswallsmith and improved dealing with original names
      [Form] Fixed variable scope when entering nested form helpers
      [Form] Added tests for blocks/templates in the format _<ID>_(widget|row|label|...)
      [Form] updated listener to check that data is an array