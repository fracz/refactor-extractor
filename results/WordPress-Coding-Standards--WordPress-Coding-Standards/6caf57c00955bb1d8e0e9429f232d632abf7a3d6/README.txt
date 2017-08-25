commit 6caf57c00955bb1d8e0e9429f232d632abf7a3d6
Author: jrfnl <jrfnl@users.noreply.github.com>
Date:   Thu Jun 15 21:15:16 2017 +0200

    Leverage the WordPress_Sniff class for (most of) the remaining sniffs

    Most WPCS sniffs extend the `WordPress_Sniff` class. However there were some which so far didn't.

    This PR changes that whenever possible.

    The affected sniffs do not necessarily *need* to use the `WordPress_Sniff` class as they don't use any of the utility methods or properties contained therein, however it will make future development slightly more intuitive as (nearly) all sniffs now follow the same pattern, as well as make life easier for the PHPCS 3.x branch which is being developed.

    The following exceptions still remain:
    * `WordPress_Sniffs_Arrays_ArrayDeclarationSniff extends Squiz_Sniffs_Arrays_ArrayDeclarationSniff` - this sniff is a partial copy of the upstream sniff with some commented out code
    * `WordPress_Sniffs_Arrays_ArrayDeclarationSpacingSniff extends Squiz_Sniffs_Arrays_ArrayDeclarationSniff` - this sniff uses the upstream sniff for the single line/multiline determination. Once the upstream sniff has been refactored into several sniffs, we may be able to decouple them. See upstream issue 582
    * `WordPress_Sniffs_NamingConventions_ValidFunctionNameSniff extends PEAR_Sniffs_NamingConventions_ValidFunctionNameSniff` - this sniff uses certain properties from the upstream sniff. We may choose to decouple this at some point by having a local copy of the relevant properties, though the maintenance burden would then also fall within WPCS.
    * `WordPress_Sniffs_NamingConventions_ValidVariableNameSniff extends PHP_CodeSniffer_Standards_AbstractVariableSniff` - uses the upstream abstract sniff logic for determination of variable vs property.