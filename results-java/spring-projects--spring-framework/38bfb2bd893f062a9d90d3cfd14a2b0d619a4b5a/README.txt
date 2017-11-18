commit 38bfb2bd893f062a9d90d3cfd14a2b0d619a4b5a
Author: Chris Beams <cbeams@vmware.com>
Date:   Tue Oct 30 10:57:12 2012 +0100

    Introduce MessageCodeFormatter abstraction

    This commit refactors changes introduced in 21760a8 as follows:

     - Introduce top-level MessageCodeFormatter interface and
       DefaultMessageCodesResolver#setMessageCodeFormatter property to allow
       for user-defined message code formatting strategies

     - Rename DefaultMessageCodesResolver.Style enum => DMCR.Format

     - Refactor DefaultMessageCodesResolver.Format to implement the new
       MessageCodeFormatter interface

    The result is that users have convenient access to common formatting
    strategies via the Format enum, while retaining the flexibility to
    provide their own custom MessageCodeFormatter implementation if desired.

    See DefaultMessageCodesResolverTests#shouldSupport*Format tests for
    usage examples.

    Issue: SPR-9707