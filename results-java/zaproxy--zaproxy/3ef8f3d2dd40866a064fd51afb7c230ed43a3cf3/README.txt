commit 3ef8f3d2dd40866a064fd51afb7c230ed43a3cf3
Author: thc202 <thc202@gmail.com>
Date:   Mon Jan 11 17:21:22 2016 +0000

    Normalise script error handling and report the message of last error

    Change class ExtensionScript to:
     - Expose/add the methods that handle script errors,
     handleScriptException(ScriptWrapper, Exception), and failure to
     implement an interface, handleFailedScriptInterface(ScriptWrapper,
     String), so that the error handling can be normalised throughout the
     code;
     - Ignore scripts that can't be enabled/disabled in the method
     setEnabled(ScriptWrapper script, boolean);
     - Reduce code duplication in setError(ScriptWrapper, Exception), by
     calling the method setError(ScriptWrapper, String) and change the
     latter to set the details of last error to the script (by calling
     ScriptWrapper.setLastErrorDetails(String)).

    Change classes ScriptsActiveScanner, ScriptsPassiveScanner and
    VariantCustom to call the exposed error handling methods, replace the
    messages used for the errors with core resource messages (the former
    messages were being read from an add-on, "Script Console", which might
    not be installed leading to MissingResourceException, though caught
    there was no indication that it happened) and to caught Exception
    instead of ScriptException and IOException (preventing script engine's
    exceptions from breaking other functionalities, for example, in case of
    errors in "Script Input Vector" scripts it would prevent the active
    scanners from being run).

    Change ScriptBasedAuthenticationMethodType to use the exposed methods
    and to not use the same try-catch block for the calls of the script
    interface instantiation and the actual calls to the interface, for better
    readability and differentiation of script error handling.

    Other "core" scripts (for example, "HTTP Sender") were already using the
    internal error handling methods from ExtensionScript, so no other
    changes are required to normalise the handling of errors.