commit ed43e165cdd6a4810570d3383156bd5b59c49e29
Author: Tyson Andre <tysonandre775@hotmail.com>
Date:   Sat Jul 15 21:39:31 2017 -0700

    Miscellaneous improvements for InvalidVariableIssetPlugin

    Don't warn about `isset($x->prop)` if that property could be valid.

    Don't erroneously warn about superglobals
    Rename the emitted issues, and change the message format for those
    issues.
    Emit `PhanPluginUndeclaredVariableInIsset` and
    `PhanPluginComplexVariableIsset` instead of `PhanUndeclaredVariable`
    in that plugin (Plugins should use different issue type names in general)