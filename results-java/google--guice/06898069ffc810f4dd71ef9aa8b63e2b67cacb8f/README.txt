commit 06898069ffc810f4dd71ef9aa8b63e2b67cacb8f
Author: limpbizkit <limpbizkit@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Sun Nov 2 05:14:55 2008 +0000

    Hopefully the last of the big exceptions refactorings. I went through all of the places we're adding context to our Errors object and made sure we're never doubling-up -- specifing the same injection point or key multiple times.

    The new errors have nice 'at' lines for parameters, fields and linked bindings. Hopefully this makes it easier to follow the stacktraces. Additional context (such as the binding's origin in a module) could be added later if desired. Currently we have module-specific sources in CreationExceptions, and plain old binding sources elsewhere. The end result is messages that can look like this:

    com.google.inject.ProvisionException: Guice provision errors:

    1) Error injecting constructor, java.lang.UnsupportedOperationException
      at com.google.inject.ProvisionExceptionTest$RealD.<init>(ProvisionExceptionTest.java:284)
      at binding for com.google.inject.ProvisionExceptionTest$RealD.class(ProvisionExceptionTest.java:284)
      at binding for com.google.inject.ProvisionExceptionTest$D.class(ProvisionExceptionTest.java:1)

    1 error

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@652 d779f126-a31b-0410-b53b-1d3aecad763e