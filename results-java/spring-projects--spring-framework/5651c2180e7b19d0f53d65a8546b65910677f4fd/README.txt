commit 5651c2180e7b19d0f53d65a8546b65910677f4fd
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Mon Nov 28 12:43:22 2016 -0500

    Further refactoring of ReactiveAdapter/Registry

    Simplify getAdapterFrom/To into a single getAdapter method that looks
    for an exact match by type first and then isAssignableFrom.

    Also expose shortcut methods in ReactiveAdapter to minimize the need
    for access to the ReactiveTypeDescriptor.

    Issue: SPR-14902