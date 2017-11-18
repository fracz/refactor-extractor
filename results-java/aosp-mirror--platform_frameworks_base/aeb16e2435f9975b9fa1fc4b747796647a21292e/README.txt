commit aeb16e2435f9975b9fa1fc4b747796647a21292e
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Tue Aug 27 18:26:48 2013 -0700

    Stronger DocumentsProvider contract.

    Using a contract class requires that a provider implement it exactly
    with little help. This change introduces a DocumentsProvider abstract
    class that provides a client-side implementation of the contract that
    greatly reduces developer burden, and improves correctness.

    This also moves to first-class DocumentRoot objects, and moves calls
    with complex side effects to be ContentProvider.call() invocations,
    offering more granular permission control over Uri operations that
    shouldn't be available through Uri grants.

    This new design also relaxes the requirement that root information be
    burned into every Uri.  Migrate ExternalDocumentsProvider and
    DocumentsUI to adopt new API.

    Bug: 10497206
    Change-Id: I6f2b3f519bfd62a9d693223ea5628a971ce2e743