commit 3b9ae92607295548df357e108fd27090cf89d1d7
Author: jrfnl <github_nospam@adviesenzo.nl>
Date:   Mon Jul 18 14:06:52 2016 +0200

    Add sniff for Restricted Database Classes.

    * Add `AbstractClassRestrictionsSniff` which can sniff for the use of (namespaced) classes. The abstract class does currently not support `use` statement aliased namespaces.
    * Refactored the `AbstractFunctionRestrictionsSniff` somewhat to make parts more reusable and extended the `AbstractClassRestrictionsSniff` from this class.
            * This refactoring includes a small efficiency improvement where the function regular expressions are no longer rebuild for every token, but only build once.
    * Add `DB/RestrictedClassesSniff` to actually sniff for the Restricted DB Classes.
    * Added extensive unit tests which test both the `DB/RestrictedClassesSniff` as well as the complete functionality of the Abstract class.

    If at some point in the future the `AbstractFunctionRestrictionsSniff` would need to support namespaces too, this will not be too hard to do. Some functions would need to be moved from the `AbstractClassRestrictionsSniff`  to the `AbstractFunctionRestrictionsSniff` and the the functionname determination would need to start using them, but the underlying logic has already been build.