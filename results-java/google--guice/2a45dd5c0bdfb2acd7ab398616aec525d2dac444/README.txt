commit 2a45dd5c0bdfb2acd7ab398616aec525d2dac444
Author: limpbizkit <limpbizkit@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Sun Jun 15 11:33:27 2008 +0000

    Massive refactoring to exception handling. I'm trying to simplify things, but they are currently a little bit more complicated. I'll do another round shortly.

    The main benefit of this change is that now all of our error handling flows through one class: Errors.java.

    It takes care of
     - managing the current source line,
     - managing the current InjectionPoint
     - building Messages
     - toStrings

    Because of this refactoring we now use almost exactly the same code for both ProvisionException and CreationException. The consequence of this is that ProvisionExceptions now include a full error report -- all of the classes injected. "Fail fast, but not too fast" now applies to Provide-time as well as Injector-create time.

    I also made InjectionPoint into a public class in SPI. It replaces dependency. I like this change because "dependency" is a very abstract name, whereas InjectionPoint is very Guicey. Guice injects stuff. Dependencies are a consequence of this, but I like the API better exposing the core Guice abstractions directly.

    This entire change needs further doc, simplification and cleanup. Todo.

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@519 d779f126-a31b-0410-b53b-1d3aecad763e