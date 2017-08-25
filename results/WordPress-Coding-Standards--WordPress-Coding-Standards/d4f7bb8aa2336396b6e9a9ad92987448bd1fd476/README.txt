commit d4f7bb8aa2336396b6e9a9ad92987448bd1fd476
Author: jrfnl <github_nospam@adviesenzo.nl>
Date:   Sat Jan 14 23:02:45 2017 +0100

    Refactor the WordPress_AbstractFunctionRestrictionsSniff

    This refactor is intended to make the WordPress_AbstractFunctionRestrictionsSniff more extendable and reusable.
    The refactor does not contain any functional changes.

    * Extend the main `WordPress_Sniff` to be able to use the `init()` function which will save us having to pass around the `$phpcsFile` and `$tokens` variables and make the utility functions contained in that class available (and allow for moving some utility functions contained in the `WordPress_AbstractClassRestrictionsSniff` to the main `WordPress_Sniff ` class).
    * Split up the logic of the sniff into three distinct functions which each individually can be overloaded in a child class.

    Additionally:
    * Added since tags to properties and functions introduced after the initial version of the class.