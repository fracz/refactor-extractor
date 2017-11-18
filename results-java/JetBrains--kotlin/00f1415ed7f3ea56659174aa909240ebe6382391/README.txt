commit 00f1415ed7f3ea56659174aa909240ebe6382391
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Fri Jul 1 16:37:46 2016 +0300

    Fix "rewrite at slice LEXICAL_SCOPE" during callable reference resolution

    Following the TODO in CallableReferencesResolutionUtils.kt, delete the
    suspicious scope and use the new resolution process with the qualifier which
    was obtained after the resolution of LHS. However, by default the tower
    resolution algorithm also considers each qualifier as a class value as well,
    which would be wrong here because resolution of LHS as a "value" happens
    earlier in DoubleColonExpressionResolver and with slightly different rules. To
    avoid that, do not mix in the "explicit receiver" scope tower processor when
    creating processors for callable reference resolution.

    Also delete unused functions and classes related to deleted scope, refactor
    Scopes.kt

     #KT-8596 Fixed