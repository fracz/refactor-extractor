commit d9ee958d885969f6ea3fd181a21615ee01711b19
Author: Chris Beams <cbeams@vmware.com>
Date:   Sat Jul 2 21:26:44 2011 +0000

    Refactor JndiPropertySource

    Prior to this change, JndiPropertySource worked directly against a JNDI
    Context instance as its 'source' object.  This works well enough, but is
    not nearly as fully-featured as Spring's existing JndiLocatorDelegate.

    This change refactors JndiPropertySource from relying on an underlying
    Context to relying on an underlying JndiLocatorDelegate.  By default,
    the delegate's "resourceRef" property is set to true, meaning that the
    implementation will always try to prepand a given name with
    "java:comp/env/" before looking up the name, and upon failure will drop
    back to the given name sans prefix.

    See JndiPropertySource Javadoc for complete details.

    Issue: SPR-8490