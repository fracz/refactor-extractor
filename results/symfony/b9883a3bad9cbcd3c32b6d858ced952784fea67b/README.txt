commit b9883a3bad9cbcd3c32b6d858ced952784fea67b
Author: Ryan Weaver <ryan@thatsquality.com>
Date:   Fri Apr 1 18:55:11 2011 -0500

    [Config] Improving the exception when a resource cannot be imported

    This improves, for example, the exception one would receive if they tried to import a resource from a bundle that doesn't exist.
    Previously, the deep "bundle is not activated" exception would be thrown. That has value, however there is no indication of where
    the exception is actually occurring.

    In this new implementation, we throw an exception that explains exactly which resource, and from which source resource, cannot be
    loaded. The deeper exception is still thrown as a nested exception.

    Two caveats:

      * The `HttpKernel::varToString` method was replicated
      * This introduces a new `Exception` class, which allows us to prevent lot's of exceptions from nesting into each other in the case
        that some deeply imported resource cannot be imported (each upstream import that fails doesn't add its own exception).