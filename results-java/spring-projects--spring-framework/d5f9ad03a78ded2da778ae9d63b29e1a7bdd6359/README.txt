commit d5f9ad03a78ded2da778ae9d63b29e1a7bdd6359
Author: Sebastien Deleuze <sdeleuze@pivotal.io>
Date:   Fri Jun 16 10:26:26 2017 +0200

    Support ScriptEngine#eval(String, Bindings) in ScriptTemplateView

    Supporting ScriptEngine#eval(String, Bindings) when no render function
    is specified allows to support use cases where script templates are
    simply evaluating a script expression with an even more simplified
    configuration.

    This improvement also makes it possible to use script engines that
    do not implement Invocable.

    Issue: SPR-15115