commit efe42cbb1f284b992d8de9f136c3b20848bee7f9
Author: Bernhard Schussek <bschussek@gmail.com>
Date:   Thu Nov 22 15:58:46 2012 +0100

    [Validator] Refactored the GraphWalker into an implementation of the Visitor design pattern.

    With this refactoring comes a decoupling of the validator from the structure of
    the underlying metadata. This way it is possible for Drupal to use the validator
    for validating their Entity API by using their own metadata layer, which is not
    modeled as classes and properties/getter methods.