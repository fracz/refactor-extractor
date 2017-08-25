commit a087e780e3163fa301c193846ef20d963977a994
Author: jrfnl <github_nospam@adviesenzo.nl>
Date:   Sat Jan 14 23:17:50 2017 +0100

    Simplify the WordPress_Sniffs_WP_DeprecatedFunctionsSniff & make it more efficient

    The refactor of the `WordPress_AbstractFunctionRestrictionsSniff` allows to make the `WordPress_Sniffs_WP_DeprecatedFunctionsSniff` more efficient.

    * No need to loop through the array and create all the error messages and subgroups for the initial `getGroups()` setup.
    * Instead, overload the parent's `process_matched_token()` method and move the logic for setting up the error message and such there, so we only execute that logic when the function has actually been matched.

    This does mean that individual deprecated function checks can no longer be excluded using the `exclude` property.
    However, the errorCode has been changed to a dynamic one which will still allow modular exclusion of one or more deprecated functions from a ruleset, though now it would need to be done using the errorCode.