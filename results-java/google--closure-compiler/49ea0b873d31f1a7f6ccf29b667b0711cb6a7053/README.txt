commit 49ea0b873d31f1a7f6ccf29b667b0711cb6a7053
Author: johnlenz <johnlenz@google.com>
Date:   Thu Mar 9 11:11:48 2017 -0800

    Improve support for refactoring JSDoc annotations:
    * Handle stacked annotations such as @const @private {Blah}
    * Add support for @exports and @package
    * Correct bad replaces for annotations that are optionally followed by a type

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=149672270